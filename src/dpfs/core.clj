(ns dpfs.core
  (:require [clojure.string :as string]
            [clojure.data.json :as json])
  (:gen-class :main true))

(def config-path (str (System/getProperty "user.home") "/.docker/config.json"))

(def valid-columns #{"ID"
                     "Image"
                     "Command"
                     "CreatedAt"
                     "RunningFor"
                     "Status"
                     "Ports"
                     "Size"
                     "Labels"
                     "Label"
                     "Mounts"
                     "Networks"
                     "Names"})

(def style-to-columns 
  {:default ["ID" "Image" "Command" "CreatedAt" "Status" "Ports" "Names"]
    :short ["Names" "Image" "Status"]})

(defn go-template [s]
  (str "{{" "." s "}}" "\\t"))

(defn get-fmt-string [& cols]
  "args should be column headers (e.g. Names, Image), outputs a psFormat table string"
  (string/join (concat ["table "] (map go-template cols))))

(defn write-ps-format [s]
  (let [config (json/read-str (slurp config-path) :key-fn str)]
    (spit 
      config-path 
      (json/write-str (conj config {"psFormat" s})))))

(defn help []
    (->> ["dpsf is a command line tool to edit how `docker ps` output is formatted"
          "You may either pass the name of a pre-determined formatting-style,"
          "or you can pass any columns in the order you'd like them to be formatted."
          "Formatting templates include:"
          (string/join " | " (map name (keys style-to-columns)))
          "Valid columns include:"
          (string/join " | " valid-columns)
          "Examples:"
          (str "dpsf " (name (first (keys style-to-columns))))
          "dpsf ID Names Labels"]
         (string/join \newline)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn valid-column? [col]
  (valid-columns (str col)))

(defn format-with-cols [cols]
  (->> cols (apply get-fmt-string) (write-ps-format)))

(defn run
  ([] (exit 0 (help)))
  ([arg] (let [help-queries #{"-h" "--help" "help"}]
          (cond
            (help-queries arg) (exit 0 (help))
            (style-to-columns (keyword arg)) (format-with-cols (style-to-columns (keyword arg)))
            (valid-column? arg) (format-with-cols [arg])
            :else (exit 1 (help)))))
  ([arg & args] (let [cols (conj args arg)]
                  (cond
                    (every? valid-column? cols) (format-with-cols cols)
                    :else (exit 1 (string/join \newline ["All args must be valid columns" (help)]))))))

(defn -main [& args] (apply run args))

;; dpsf -> help
;; dpsf help -> help
;; dpsf default
;; dpsf Names
;; dpsf Names Image
;; dpsf columns
;; dpsf styles

