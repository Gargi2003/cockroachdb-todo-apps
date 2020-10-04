(ns clojure-todoapp-azetestuser1.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [reagent-material-ui.colors :as colors]
   [reagent-material-ui.core.box :refer [box]]
   [reagent-material-ui.core.button :refer [button]]
   [reagent-material-ui.core.chip :refer [chip]]
   [reagent-material-ui.core.css-baseline :refer [css-baseline]]
   [reagent-material-ui.core.grid :refer [grid]]
   [reagent-material-ui.core.menu-item :refer [menu-item]]
   [reagent-material-ui.core.text-field :refer [text-field]]
   [reagent-material-ui.core.list :refer [list]]
   [reagent-material-ui.core.list-item :refer [list-item]]
   [reagent-material-ui.core.list-item-text :refer [list-item-text]]
   [reagent-material-ui.core.checkbox :refer [checkbox]]
   [reagent-material-ui.core.textarea-autosize :refer [textarea-autosize]]
   [reagent-material-ui.core.toolbar :refer [toolbar]]
   [reagent-material-ui.icons.add-box :refer [add-box]]
   [reagent-material-ui.icons.clear :refer [clear]]
   [reagent-material-ui.icons.face :refer [face]]
   [reagent-material-ui.pickers.date-picker :refer [date-picker]]
   [reagent-material-ui.pickers.mui-pickers-utils-provider :refer [mui-pickers-utils-provider]]
   [reagent-material-ui.styles :as styles]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/items"
     ["" :items]
     ["/:item-id" :item]]
    ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;;--------------------------
;; Data Atom
(defonce app-state (atom { 
                                 1 {:is-done true
                                      :todo-text "something intertesting"
                                      :has-changed false
                                      }
                             
                                 2 {:is-done false
                                      :todo-text "hello world!!"
                                      :has-changed false
                                      }
                                 }))

;;--------------------------
;; Helper Code
(defn event-value
  [^js/Event e]
  (.. e -target -value))

(defn load-item []
  [

(defn add-item []
  [grid {:item true :xs 3}
   [button {:color "primary" :variant "contained" :on-click #(swap! app-state assoc (+ 1 (apply max (keys @app-state))) {:is-done false :todo-text "add todo" :has-changed true })}  "add item"]]
  )

(defn todo-item [id todo-detail]
  (let [{:keys [is-done todo-text]} todo-detail]
    [list-item {:lid id :key id}
     [checkbox {:checked is-done :chckid id
                :on-change #(swap! app-state assoc id  {:is-done (not is-done) :todo-text todo-text :has-changed true })
                }]
     [text-field {:variant "outlined" :defaultValue todo-text :txtid id
                  :on-change (fn [e] (swap! app-state assoc id {:is-done is-done :todo-text (event-value e) :has-changed true}))
                  }]
     ]))

(defn todo-list [todos]
  (do
    (println todos)
  [list
  (map
   (fn [id] (todo-item id (todos id)))
   (keys todos))]
  ))
;; -------------------------
;; Page components

(defn home-page []
  (fn []
    [:span.main
     [:h1 "Todo List "]
     [:div
      [grid {:container true :spacing 1}
       (add-item)
       [grid {:item true :xs 3}
        [button {:color "primary" :variant "contained"} "load list"]]
       [grid {:item true :xs 6} ]
           (todo-list @app-state)
       ]]]))



(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of clojure_todoapp_azetestuser1"]
     [:ul (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))


(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of clojure_todoapp_azetestuser1")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))


(defn about-page []
  (fn [] [:span.main
          [:h1 "About clojure_todoapp_azetestuser1"]]))


;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :items #'items-page
    :item #'item-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header]
       [page]
       [:footer]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
