(ns cli-test.core
  (:require [clojure.string :as string]
            [clojure.data.json :as json])
  (:gen-class :main true))

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

(def style-to-columns {:default ["ID" "Image" "Command" "CreatedAt" "Status" "Ports" "Names"]
                        :short ["Names" "Image" "Status"]})

(defn go-template-property [s]
  (str "{{" "." s "}}" "\\t"))

(defn get-fmt-string [& args]
  "takes a list of column headers, (e.g. Names, Image) and outputs a psFormat table string"
  (let [prefix "table "]
    (string/join (concat [prefix] (map go-template-property args)))))

(defn update-ps-format [s]
  (let [path (str (System/getProperty "user.home") "/.docker/config.json")
        config (json/read-str (slurp path) :key-fn str)]
    (println s)
    (spit 
      path 
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

(defn run
  ([] (exit 0 (help)))
  ([arg] (let [help-queries #{"-h" "--help" "help"}]
          (cond
            (help-queries arg) (exit 0 (help))
            (style-to-columns (keyword arg)) (->> (style-to-columns (keyword arg))
                                                  (apply get-fmt-string)
                                                  (update-ps-format))
            (valid-column? arg) (update-ps-format (get-fmt-string arg))
            :else (exit 1 (help)))))
  ([arg & args] (let [cols (conj args arg)]
                  (cond
                    (every? valid-column? cols) (update-ps-format (apply get-fmt-string cols))
                    :else (exit 1 (string/join \newline ["All args must be valid columns" (help)]))))))

(defn -main [& args] (apply run args))

;; dpsf -> help
;; dpsf help -> help
;; dpsf default
;; dpsf Names
;; dpsf Names Image
;; dpsf columns
;; dpsf styles
