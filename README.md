# QRCode-FacialRecog-Attendance-System
Dynamically changes QR codes every 10 seconds to combat fraud.  
Facial Recognition implemented using prebuilt FaceNet Deep learning model.  
  
Project Flow Chart  
  
![Untitled Diagram drawio](https://user-images.githubusercontent.com/51827238/234007223-90fadca4-7d3a-4be9-88b8-7f1abdcd330f.png)

FOLDERS

qrAppNode folder
-> Node.js project source code

QRVersion11
-> Android Studio project source code

------------------------------------------------------------------------------------------

SOFTWARES REQUIRED

Android Studio
Visual Studio Code /any other code editor
ngrok
MongoDB (preferrably MongoDBCompass is well)

------------------------------------------------------------------------------------------

HOW TO RUN PROJECT?

open qrAppNode folder in VSCode terminal or simply in command line
execute below commands

> npm install
> node server.js

server is hosted locally on PORT 3000 by default.
open browser and go to 
"localhost:3000"

to host the server on the internet, use ngrok
Search Google for ngrok, and download it
Signup on ngrok through there official website, make your account, an "authorization token" is provided.

Run ngrok.exe as administrator
execute the command
"ngrok config add-authtoken <YOUR_TOKEN>"

now execute
"ngrok.exe http 3000"

It will provide a public forwarding url. Copy this url.
The website is now publically hosted on the internet.

Now, open the specified folder "QRVersion11" in Android Studio.
open the class file "MyUrl.kt"
replace the url with the one that you copied from ngrok.exe

Now, to run the Android App on your smartphone, there are mainly two ways:
-> by wireless debugging mode, (refer Google Search)
-> by exporting the apk file and transferring the file to the Android phone and installing it. (refer Google Search)




Android Application (Kotlin)  
  
![Picture3](https://user-images.githubusercontent.com/51827238/234006667-b8d17974-bbe9-4ee3-aede-b468c8f53dd0.png)

![Picture4](https://user-images.githubusercontent.com/51827238/234006687-38e79826-efb4-405d-b2c8-2db7c5fb1bdc.jpg)  

![Picture5](https://user-images.githubusercontent.com/51827238/234006703-b3f882bd-dad1-46a0-98e4-d67176259945.jpg)

Web Portal (Node.js)

![Screenshot 2023-04-17 133700](https://user-images.githubusercontent.com/51827238/234007793-a4fcc1bf-d08b-4415-8b18-8624d5f01581.png)

![Screenshot 2023-04-17 133810](https://user-images.githubusercontent.com/51827238/234007805-5616e9e1-1df3-453f-b078-5393bc07b803.png)

![Picture1](https://user-images.githubusercontent.com/51827238/234006971-aaa787f1-4f68-4e4d-a6fa-ec74b2f0d57f.jpg)
