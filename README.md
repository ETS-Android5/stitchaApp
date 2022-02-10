# 📱 Stitcha: Android Metro Voting App 
## 🖼️ Screenshots
### 🧑 Client
--------------
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/client0.png"> 
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/client2.png">
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/client3.png">
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/client4.png"> 

### 🖥️ Server (DB)
--------------
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/db1.png"> 
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/db2.png">
<img src="https://github.com/fayssalElAnsari/stitchaApp/edit/master/pics/db3.png"> 

## ℹ️ Description
  This repository contains an application template for a voting system, the exmaple we have is that of voting app on whether a certain station is working or not, if you open the app for the first time you will be asked to give the required permission if declined two times the app will close, after giving the permissions you wil be automatically redirected to a register/login screen, we have two options enabled by default: google login and facebook login. After a successful login you wil be automatically redirected to the main screen of the app containing a map, the map will autozoom on the city you are in currently (the location between you and all the cities will be calculated and the nearest city is the city to be loaded). The pins of each metro station will autoload if you click on a pin a Toast message will show up telling you how many votes have been made in a specified duration, votes are deleted after 24h. You have two options to vote from `working` and `not working` if the user votes a Toast will show up and the info is automatically updated on the database and therefore for all connected users.   
  If the user clicks on the app's `logo` a live feed of all the votes in the last 24h is shown (this vote could be for anything, not necessarily for telling if the metro is working or not). The feed is composed of a recyclerview with autoupdating cards each card is colored with the same color as the voting buttons (positive `green` and negative `red`). The cards contain information about the map pin voted on and information on the user who voted and the time of vote.  
  
## ✔️ What have been achieved during the development of this project 
* [x] The construction of a functional `realtime database` voting metro `android application`
* [x] The use of `firebase firestore` and `firebase realtime database` in order to keep track  
      of votes of each city and each metro station in realtime
* [x] login functionality using `facebook`, `google` and `email` with the help of `firebase`
* [x] Followed basic UI/UX principles in order to make a simple interface...
* [x] Using `RecyclerView` and `CardView` to show the list of items
* [x] Implemented a `loading screen` (splash screen), and a `login screen`.
* [x] The management of `app permissions` (fine location, coarse location,...)
* [x] Using `backgroud services` in order to track location while app is in the background
* [x] Used Google maps API to show and edit a `map view`
* [x] `Autozoom`, `load pins`, `update info` and `define geofences` on a google maps view
* [x] A `reactive UI` built from scratch with the help of `firestore` and `firebase realtime db`
* [x] `Firebase analytics` to keep track of user engagement in realtime

# 🐤 Getting Started 
## 👶 Prerequisites 
* <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Android_Studio_Icon_%282014-2019%29.svg/1200px-Android_Studio_Icon_%282014-2019%29.svg.png" width="25">  Android Studio - The official integrated development environment (IDE) for Android application development.

## 🚅 How To Use This Repo 
* Clone the repository with:
```console
fayssal@mypc:~$ git clone https://github.com/fayssalElAnsari/stitchaApp.git
```
* Import the project into `Android Studio`
* make your own modifications to whatever you want 😅
* Put your `google-services.json` file in the project
* PS: If the database doesn't exist it should be created automatically
* Export your `android` app 

# 🛠️ Built With 
* <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Android_Studio_Icon_%282014-2019%29.svg/1200px-Android_Studio_Icon_%282014-2019%29.svg.png" width="25">  Android Studio - The official integrated development environment (IDE) for Android application development.
* <img src="https://images.vexels.com/media/users/3/166401/isolated/lists/b82aa7ac3f736dd78570dd3fa3fa9e24-java-programming-language-icon.png" width="25">  Java - A high-level, class-based, object-oriented programming language that is designed to have as few implementation dependencies as possible.
* <img src="https://e7.pngegg.com/pngimages/119/167/png-clipart-firebase-cloud-messaging-google-developers-software-development-kit-google-angle-triangle-thumbnail.png" width="25">  Firebase - A Backend-as-a-Service (Baas). It provides developers with a variety of tools and services to help them develop quality apps, grow their user base, and earn profit
* <img src="https://iconape.com/wp-content/files/wa/374543/png/374543.png" width="25">  AdMob - A free platform that gives you a way to earn money by displaying targeted ads alongside your app content
* <img src="https://cdn.iconscout.com/icon/free/png-256/xml-file-2330558-1950399.png" width="25">  XML - A markup language and file format for storing, transmitting, and reconstructing arbitrary data

# 📖 Author 
* Fayssal EL ANSARI

# 👮 License 
* Attribution-NonCommercial-ShareAlike 4.0 International
