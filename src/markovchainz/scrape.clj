(ns markovchainz.scrape
  [:require
   [org.httpkit [client :as http]]
   [net.cgrand.enlive-html :as html]
   [markovchainz [redis :as redis]]
   [clojure [string :as str]]]
  [:import
   [java.io ByteArrayInputStream]])

(def song-set [
 "http://rapgenius.com/Drake-all-me-lyrics"
 "http://rapgenius.com/A-ap-rocky-fuckin-problems-lyrics"
 "http://rapgenius.com/2-chainz-and-wiz-khalifa-we-own-it-fast-and-furious-lyrics"
 "http://rapgenius.com/2-chainz-feds-watching-lyrics"
 "http://rapgenius.com/2-chainz-i-do-it-lyrics"
 "http://rapgenius.com/Bob-headband-lyrics"
 "http://rapgenius.com/Kanye-west-mercy-lyrics"
 "http://rapgenius.com/Mr-bangladesh-100-lyrics"
 "http://rapgenius.com/Nelly-100k-lyrics"
 "http://rapgenius.com/2-chainz-2-chainz-sway-in-the-morning-freestyle-lyrics"
 "http://rapgenius.com/Boy-wonder-2-of-everything-lyrics"
 "http://rapgenius.com/2-chainz-36-lyrics"
 "http://rapgenius.com/Rick-ross-911-remix-lyrics"
 "http://rapgenius.com/2-chainz-addicted-to-rubberbands-lyrics"
 "http://rapgenius.com/Game-ali-bomaye-lyrics"
 "http://rapgenius.com/Trinidad-james-all-gold-everything-remix-lyrics"
 "http://rapgenius.com/Drake-all-me-lyrics"
 "http://rapgenius.com/Greekdagod-all-me-remix-lyrics"
 "http://rapgenius.com/Yungbrezzy-alot-of-diss-and-dat-lyrics"
 "http://rapgenius.com/Cyhi-the-prynce-a-town-remix-lyrics"
 "http://rapgenius.com/Tyga-bad-bitches-remix-lyrics"
 "http://rapgenius.com/Wale-bait-remix-lyrics"
 "http://rapgenius.com/Juicy-j-bandz-a-make-her-dance-remix-lyrics"
 "http://rapgenius.com/Gudda-gudda-bang-bang-lyrics"
 "http://rapgenius.com/2-chainz-beautiful-pain-lyrics"
 "http://rapgenius.com/Wale-been-too-long-lyrics"
 "http://rapgenius.com/Nicki-minaj-beez-in-the-trap-lyrics"
 "http://rapgenius.com/2-chainz-bet-uncut-cypher-lyrics"
 "http://rapgenius.com/T-pain-big-man-lyrics"
 "http://rapgenius.com/2-chainz-birthday-song-lyrics"
 "http://rapgenius.com/2-chainz-black-unicorn-lyrics"
 "http://rapgenius.com/2-chainz-boo-lyrics"
 "http://rapgenius.com/Justin-bieber-boyfriend-remix-lyrics"
 "http://rapgenius.com/Kreayshawn-breakfast-syrup-lyrics"
 "http://rapgenius.com/Major-lazer-bubble-butt-lyrics"
 "http://rapgenius.com/Ace-hood-bugatti-remix-lyrics"
 "http://rapgenius.com/Bangladesh-buy-lyrics"
 "http://rapgenius.com/2-chainz-call-tiesha-lyrics"
 "http://rapgenius.com/2-chainz-cant-do-it-like-me-lyrics"
 "http://rapgenius.com/Curren-y-capitol-lyrics"
 "http://rapgenius.com/Yo-gotti-cases-lyrics"
 "http://rapgenius.com/Booba-cest-la-vie-lyrics"
 "http://rapgenius.com/Booba-cest-la-vie-english-version-lyrics"
 "http://rapgenius.com/J-cole-chris-tucker-lyrics"
 "http://rapgenius.com/2-chainz-countdown-lyrics"
 "http://rapgenius.com/Jadakiss-count-it-lyrics"
 "http://rapgenius.com/Jeezy-count-it-up-lyrics"
 "http://rapgenius.com/Nelly-country-ass-nigga-lyrics"
 "http://rapgenius.com/2-chainz-cowboy-lyrics"
 "http://rapgenius.com/2-chainz-crack-lyrics"
 "http://rapgenius.com/The-gift-mattabatta-crustacean-lyrics"
 "http://rapgenius.com/Lil-wayne-days-and-days-lyrics"
 "http://rapgenius.com/Lil-wayne-days-and-days-french-version-lyrics"
 "http://rapgenius.com/Gucci-mane-dirty-cup-lyrics"
 "http://rapgenius.com/2-chainz-dirty-dark-lyrics"
 "http://rapgenius.com/Tyga-do-my-dance-lyrics"
 "http://rapgenius.com/8ball-dont-bring-me-down-lyrics"
 "http://rapgenius.com/2-chainz-dope-peddler-lyrics"
 "http://rapgenius.com/Kirko-bangz-drank-in-my-cup-remix-lyrics"
 "http://rapgenius.com/2-chainz-employee-of-the-month-lyrics"
 "http://rapgenius.com/Sean-paul-entertainment-lyrics"
 "http://rapgenius.com/Sean-paul-entertainment-remix-lyrics"
 "http://rapgenius.com/2-chainz-extra-lyrics"
 "http://rapgenius.com/2-chainz-extremely-blessed-lyrics"
 "http://rapgenius.com/Hit-boy-fan-remix-lyrics"
 "http://rapgenius.com/2-chainz-feds-watching-lyrics"
 "http://rapgenius.com/Lil-wayne-feds-watching-lyrics"
 "http://rapgenius.com/2-chainz-feeling-you-lyrics"
 "http://rapgenius.com/Bun-b-fire-lyrics"
 "http://rapgenius.com/Tls-fkin-problems-remix-lyrics"
 "http://rapgenius.com/2-chainz-flossin-lyrics"
 "http://rapgenius.com/2-chainz-fork-lyrics"
 "http://rapgenius.com/Meek-mill-freak-show-lyrics"
 "http://rapgenius.com/Future-freebandz-lyrics"
 "http://rapgenius.com/Rick-ross-fuck-em-lyrics"
 "http://rapgenius.com/Kevin-mccall-fucking-problems-remix-lyrics"
 "http://rapgenius.com/A-ap-rocky-fuckin-problems-lyrics"
 "http://rapgenius.com/Trey-songz-fuckin-problems-freestyle-lyrics"
 "http://rapgenius.com/A-ap-rocky-fuckin-problems-remix-lyrics"
 "http://rapgenius.com/Planet-vi-fuck-you-too-lyrics"
 "http://rapgenius.com/2-chainz-fuk-da-roof-lyrics"
 "http://rapgenius.com/Psy-gangnam-style-diplo-remix-lyrics"
 "http://rapgenius.com/2-chainz-gasolean-lyrics"
 "http://rapgenius.com/Gucci-mane-get-it-back-lyrics"
 "http://rapgenius.com/2-chainz-get-it-in-lyrics"
 "http://rapgenius.com/Wale-getmedoe-lyrics"
 "http://rapgenius.com/Wale-getmedoe-french-version-lyrics"
 "http://rapgenius.com/Young-dolph-get-this-money-lyrics"
 "http://rapgenius.com/2-chainz-ghetto-dreams-lyrics"
 "http://rapgenius.com/2-chainz-ghetto-dreams-french-version-lyrics"
 "http://rapgenius.com/Robin-thicke-give-it-2-u-remix-lyrics"
 "http://rapgenius.com/Wale-globetrotter-lyrics"
 "http://rapgenius.com/Clinton-sparks-gold-rush-lyrics"
 "http://rapgenius.com/2-chainz-good-morning-lyrics"
 "http://rapgenius.com/2-chainz-got-one-lyrics"
 "http://rapgenius.com/Lil-wayne-grew-up-a-screw-up-lyrics"
 "http://rapgenius.com/Yg-grind-mode-lyrics"
 "http://rapgenius.com/Swizz-beatz-hands-up-lyrics"
 "http://rapgenius.com/Juicy-j-having-sex-lyrics"
 "http://rapgenius.com/Bob-headband-lyrics"
 "http://rapgenius.com/Youngjuiceman-hella-bands-lyrics"
 "http://rapgenius.com/Tyga-hijack-lyrics"
 "http://rapgenius.com/2-chainz-hip-hop-awards-uncut-cypher-lyrics"
 "http://rapgenius.com/Dj-scream-hood-rich-anthem-lyrics"
 "http://rapgenius.com/Dj-scream-hood-rich-anthem-remix-lyrics"
 "http://rapgenius.com/Ludacris-i-aint-the-one-lyrics"
 "http://rapgenius.com/2-chainz-i-do-it-lyrics"
 "http://rapgenius.com/2-pistols-i-dont-care-lyrics"
 "http://rapgenius.com/Dj-khaled-i-dont-see-em-lyrics"
 "http://rapgenius.com/2-chainz-i-feel-good-lyrics"
 "http://rapgenius.com/2-chainz-i-got-it-lyrics"
 "http://rapgenius.com/2-chainz-i-luv-dem-strippers-lyrics"
 "http://rapgenius.com/Bow-wow-ima-stunt-lyrics"
 "http://rapgenius.com/2-chainz-im-different-lyrics"
 "http://rapgenius.com/2-chainz-im-different-french-version-lyrics"
 "http://rapgenius.com/Drumma-boy-im-on-worldstar-lyrics"
 "http://rapgenius.com/Gucci-mane-im-up-lyrics"
 "http://rapgenius.com/2-chainz-in-town-lyrics"
 "http://rapgenius.com/Wiz-khalifa-its-nothin-lyrics"
 "http://rapgenius.com/2-chainz-kesha-lyrics"
 "http://rapgenius.com/2-chainz-kitchen-remix-lyrics"
 "http://rapgenius.com/2-chainz-ko-lyrics"
 "http://rapgenius.com/B-smyth-leggo-lyrics"
 "http://rapgenius.com/Red-cafe-let-it-go-remix-lyrics"
 "http://rapgenius.com/2-chainz-letter-to-da-rap-game-lyrics"
 "http://rapgenius.com/Roscoe-dash-like-diz-lyrics"
 "http://rapgenius.com/2-chainz-like-me-lyrics"
 "http://rapgenius.com/2-chainz-live-and-learn-it-will-lyrics"
 "http://rapgenius.com/2-chainz-livin-lyrics"
 "http://rapgenius.com/Ti-loud-mouth-lyrics"
 "http://rapgenius.com/Ace-hood-luv-her-lyrics"
 "http://rapgenius.com/2-chainz-mainstream-ratchet-lyrics"
 "http://rapgenius.com/Young-fame-make-it-work-lyrics"
 "http://rapgenius.com/Sterling-simms-make-you-somebody-lyrics"
 "http://rapgenius.com/French-montana-marble-floors-lyrics"
 "http://rapgenius.com/Game-mean-muggin-lyrics"
 "http://rapgenius.com/Kanye-west-mercy-lyrics"
 "http://rapgenius.com/Future-mind-blown-lyrics"
 "http://rapgenius.com/2-chainz-money-machine-lyrics"
 "http://rapgenius.com/Big-krit-money-on-the-floor-lyrics"
 "http://rapgenius.com/Big-sean-mula-remix-lyrics"
 "http://rapgenius.com/2-chainz-murder-lyrics"
 "http://rapgenius.com/Dj-drama-my-moment-lyrics"
 "http://rapgenius.com/R-kelly-my-story-lyrics"
 "http://rapgenius.com/Freddie-gibbs-neighborhood-hoez-lyrics"
 "http://rapgenius.com/2-chainz-netflix-lyrics"
 "http://rapgenius.com/2-chainz-no-lie-lyrics"
 "http://rapgenius.com/Adolescent-no-lie-lyrics"
 "http://rapgenius.com/Dj-drama-oh-my-remix-lyrics"
 "http://rapgenius.com/Dj-drama-oh-my-remix-ft-trey-songz-2-chainz-and-big-sean-lyrics"
 "http://rapgenius.com/Juicy-j-oh-well-remix-lyrics"
 "http://rapgenius.com/Chris-brown-oh-yeahhhh-lyrics"
 "http://rapgenius.com/Gucci-mane-okay-with-me-lyrics"
 "http://rapgenius.com/2-chainz-one-day-at-a-time-lyrics"
 "http://rapgenius.com/2-chainz-outroduction-lyrics"
 "http://rapgenius.com/Jeremiah-outta-control-lyrics"
 "http://rapgenius.com/2-chainz-own-drugs-od-lyrics"
 "http://rapgenius.com/Pill-pacman-remix-lyrics"
 "http://rapgenius.com/Stalley-party-heart-lyrics"
 "http://rapgenius.com/Bob-perfect-symmetry-lyrics"
 "http://rapgenius.com/2-chainz-pimp-c-back-lyrics"
 "http://rapgenius.com/Big-krit-pimps-lyrics"
 "http://rapgenius.com/Travis-porter-pussy-real-good-lyrics"
 "http://rapgenius.com/Lil-wayne-real-as-they-come-lyrics"
 "http://rapgenius.com/Lil-wayne-rich-as-fuck-lyrics"
 "http://rapgenius.com/Lil-wayne-rich-as-fuck-french-version-lyrics"
 "http://rapgenius.com/Lil-freezy-rich-as-fuck-remix-lyrics"
 "http://rapgenius.com/Jesse-v-ring-ring-remix-lyrics"
 "http://rapgenius.com/2-chainz-riot-lyrics"
 "http://rapgenius.com/50-cent-riot-remix-lyrics"
 "http://rapgenius.com/Jeezy-rip-lyrics"
 "http://rapgenius.com/Jeezy-rip-another-remix-lyrics"
 "http://rapgenius.com/Lil-freezy-rip-remix-lyrics"
 "http://rapgenius.com/Waka-flocka-flame-rooster-in-my-rari-remix-lyrics"
 "http://rapgenius.com/Wale-rotation-lyrics"
 "http://rapgenius.com/Ludacris-secret-song-lyrics"
 "http://rapgenius.com/Dj-scream-shinin-remix-lyrics"
 "http://rapgenius.com/Sean-kingston-shotta-luv-lyrics"
 "http://rapgenius.com/2-chainz-slangin-birds-lyrics"
 "http://rapgenius.com/Ray-jr-sloppy-remix-lyrics"
 "http://rapgenius.com/Young-buck-so-gone-lyrics"
 "http://rapgenius.com/2-chainz-so-we-can-live-lyrics"
 "http://rapgenius.com/2-chainz-spend-it-lyrics"
 "http://rapgenius.com/Rick-ross-spend-it-remix-lyrics"
 "http://rapgenius.com/2-chainz-spend-it-remix-lyrics"
 "http://rapgenius.com/2-chainz-stand-still-lyrics"
 "http://rapgenius.com/Kid-ink-stop-lyrics"
 "http://rapgenius.com/2-chainz-stop-me-now-lyrics"
 "http://rapgenius.com/Meek-mill-str8-like-dat-lyrics"
 "http://rapgenius.com/2-chainz-stunt-lyrics"
 "http://rapgenius.com/Jeezy-supafreak-lyrics"
 "http://rapgenius.com/Ciara-sweat-lyrics"
 "http://rapgenius.com/Nore-tadow-lyrics"
 "http://rapgenius.com/Jason-derulo-talk-dirty-lyrics"
 "http://rapgenius.com/Raekwon-the-morning-lyrics"
 "http://rapgenius.com/Kanye-west-the-one-lyrics"
 "http://rapgenius.com/E-40-they-point-lyrics"
 "http://rapgenius.com/2-chainz-think-about-it-lyrics"
 "http://rapgenius.com/King-louie-too-cool-remix-lyrics"
 "http://rapgenius.com/2-chainz-too-easy-lyrics"
 "http://rapgenius.com/Tinie-tempah-trampoline-lyrics"
 "http://rapgenius.com/The-dream-turnt-lyrics"
 "http://rapgenius.com/2-chainz-turnt-up-freestyle-lyrics"
 "http://rapgenius.com/2-chainz-turn-up-lyrics"
 "http://rapgenius.com/2-chainz-twilight-zone-lyrics"
 "http://rapgenius.com/2-chainz-u-da-realest-lyrics"
 "http://rapgenius.com/Busta-rhymes-uncut-cypher-lyrics"
 "http://rapgenius.com/2-chainz-undastatement-lyrics"
 "http://rapgenius.com/Rocko-uoeno-extended-remix-lyrics"
 "http://rapgenius.com/Boom-man-up-in-here-lyrics"
 "http://rapgenius.com/2-chainz-up-in-smoke-lyrics"
 "http://rapgenius.com/Travis-scott-upper-echelon-lyrics"
 "http://rapgenius.com/2-chainz-used-2-lyrics"
 "http://rapgenius.com/Gucci-use-me-lyrics"
 "http://rapgenius.com/Gucci-mane-use-me-lyrics"
 "http://rapgenius.com/2-chainz-vi-agra-lyrics"
 "http://rapgenius.com/Ludacris-we-got-lyrics"
 "http://rapgenius.com/2-chainz-and-wiz-khalifa-we-own-it-fast-and-furious-lyrics"
 "http://rapgenius.com/Fabolous-when-i-feel-like-it-lyrics"
 "http://rapgenius.com/2-chainz-where-u-been-lyrics"
 "http://rapgenius.com/French-montana-whip-lyrics"
 "http://rapgenius.com/Pusha-t-who-i-am-lyrics"
 "http://rapgenius.com/Machine-gun-kelly-wild-boy-remix-lyrics"
 "http://rapgenius.com/Wild-n-out-cast-wildstyle-503-lyrics"
 "http://rapgenius.com/2-chainz-wut-we-doin-lyrics"
 "http://rapgenius.com/2-chainz-yall-aint-lyrics"
 "http://rapgenius.com/Cory-gunz-yall-aint-got-nothin-on-me-lyrics"
 "http://rapgenius.com/David-banner-yao-ming-lyrics"
 "http://rapgenius.com/David-banner-yao-ming-leaked-remix-lyrics"
 "http://rapgenius.com/2-chainz-yuck-lyrics"
 "http://rapgenius.com/Juicy-j-zip-and-a-double-cup-remix-lyrics"
 "http://rapgenius.com/2-chainz-10-summaz-lyrics"
 "http://rapgenius.com/2-chainz-2-chaniz-vs-akon-lyrics"
 "http://rapgenius.com/2-chainz-all-i-do-is-me-lyrics"
 "http://rapgenius.com/Young-deshawn-am-strappin-for-the-streets-lyrics"
 "http://rapgenius.com/Lil-bozo-a-peal-lyrics"
 "http://rapgenius.com/Nicki-minaj-beez-in-a-trap-lyrics"
 "http://rapgenius.com/Lil-deshawn-bout-my-money-lyrics"
 "http://rapgenius.com/Major-lazer-bubble-butt-remix-lyrics"
 "http://rapgenius.com/Jay-stonez-cooking-all-morning-lyrics"
 "http://rapgenius.com/2-chainz-dont-bring-me-down-lyrics"
 "http://rapgenius.com/2-chainz-dope-pedder-eftw-version-lyrics"
 "http://rapgenius.com/I-20-fightin-in-the-club-lyrics"
 "http://rapgenius.com/Xtreme-mcphilly-foreign-lyrics"
 "http://rapgenius.com/Wale-get-me-doe-lyrics"
 "http://rapgenius.com/Sammie-gettin-em-lyrics"
 "http://rapgenius.com/2-chainz-goodnight-lyrics"
 "http://rapgenius.com/Lil-flip-i-came-to-bring-the-pain-lyrics"
 "http://rapgenius.com/2-chainz-im-differen-lyrics"
 "http://rapgenius.com/Artie-sosa-im-different-freestyle-lyrics"
 "http://rapgenius.com/Gudda-gudda-im-gudda-lyrics"
 "http://rapgenius.com/Papoose-im-like-that-remix-lyrics"
 "http://rapgenius.com/Young-money-smith-im-so-fly-instrumental-lyrics"
 "http://rapgenius.com/2-chainz-intro-lyrics"
 "http://rapgenius.com/Wiz-khalifa-its-nothin-french-version-lyrics"
 "http://rapgenius.com/Cap-1-i-want-sum-lyrics"
 "http://rapgenius.com/2-chainz-la-la-remix-lyrics"
 "http://rapgenius.com/Lusive-levitate-lyrics"
 "http://rapgenius.com/David-banner-like-my-daddyyao-ming-lyrics"
 "http://rapgenius.com/Lil-wayne-lil-wayne-raf-lyrics"
 "http://rapgenius.com/Mayday-veni-vidi-vici-mayday-stylin-onem-lyrics"
 "http://rapgenius.com/Antonio-aaron-monster-lyrics"
 "http://rapgenius.com/R-kelly-my-story-eftw-edition-lyrics"
 "http://rapgenius.com/Eric-benet-news-for-you-remix-lyrics"
 "http://rapgenius.com/2-chainz-no-lie-remix-lyrics"
 "http://rapgenius.com/Bangladesh-phantom-lyrics"
 "http://rapgenius.com/Baby-w-problems-freestyle-lyrics"
 "http://rapgenius.com/Chingy-represent-lyrics"
 "http://rapgenius.com/I-20-slum-lyrics"
 "http://rapgenius.com/2-chainz-supafly-lyrics"
 "http://rapgenius.com/Kirko-bangz-top-floor-lyrics"
 "http://rapgenius.com/Fabolous-when-i-feel-like-it-exclusive-lyrics"
 "http://rapgenius.com/Wiz-khalifa-work-hard-play-hard-all-day-everyday-lyrics"
 "http://rapgenius.com/Roxxan-xman15-lyrics"
])

(defn html-string-to-enlive
  [html]
  (let [bytes (ByteArrayInputStream. (.getBytes html "UTF-8"))]
    (html/html-resource bytes)))

(defn get-lyrics-blob
  [url]
  (:content (first (html/select
                    (html-string-to-enlive (:body @(http/get url {:max-redirects 10})))
                    [:.lyrics]))))

(defn flatten-annotated-lyrics [blob]
  (loop [b blob]
    (let [lyrics (flatten (map #(if (map? %) (:content %) %) b))]
      (if (= b lyrics)
        lyrics
        (recur lyrics)))))

(defn extract-words [blob]
  (let [text (filter #(not (map? %)) (flatten-annotated-lyrics blob))
        compacted-text (filter identity text)
        trimmed (map str/trim compacted-text)
        lines (filter not-empty trimmed)
        good-lines (filter #(not (re-find #"[\[\]{}]" %)) lines)]
    good-lines))


(defn get-title [url]
  (:content (first (html/select
                    (html-string-to-enlive (:body @(http/get url)))
                    [:.label.edit_song_description :> :i]))))

(defn extract-title [blob]
  (last (clojure.string/split
         (first blob) #":")))

(defn song-map [url]
  (let [lyrics (extract-words (get-lyrics-blob url))
        title (extract-title (get-title url))]
    {:title title :lyrics lyrics :url url}))

(defn yuck []
  (song-map "http://rapgenius.com/2-chainz-yuck-lyrics"))

(defn birthday []
  (song-map "http://rapgenius.com/2-chainz-birthday-song-lyrics"))

(defn no-lie []
  (song-map "http://rapgenius.com/2-chainz-no-lie-lyrics"))

(defn save-songs
  ([urls] (doall (pmap #(redis/add-to-set "songs" (song-map %)) urls)))
  ([] (save-songs song-set)))
