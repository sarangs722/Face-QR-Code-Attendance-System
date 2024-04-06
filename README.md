# QRCode-FacialRecog-Attendance-System
Dynamically changes QR codes every 10 seconds to combat fraud.  
Facial Recognition implemented using prebuilt FaceNet Deep learning model.  
  
### Project Flow Chart  


![Screenshot 2023-04-17 130326](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/8a81cb9b-38be-48b6-b4d8-de3b84d4bffb)

![Untitled Diagram drawio](https://user-images.githubusercontent.com/51827238/234007223-90fadca4-7d3a-4be9-88b8-7f1abdcd330f.png)

### Folders

qrAppNode folder
-> Node.js project source code

QRVersion11
-> Android Studio project source code

------------------------------------------------------------------------------------------

### Softwares required

- Android Studio
- Visual Studio Code /any other code editor
- ngrok
- MongoDB (preferrably MongoDBCompass is well)

------------------------------------------------------------------------------------------

##### Hardware Requirements
- Android Smartphone with API Level 28 and above

------------------------------------------------------------------------------------------

## How to run the project? 

open qrAppNode folder in VSCode terminal or simply in command line
execute below commands

```javascript
npm install
node server.js
```

server is hosted locally on PORT 3000 by default.
open browser and go to 
```
localhost:3000
```

to host the server on the internet, use ngrok
Search Google for ngrok, and download it
Signup on ngrok through there official website, make your account, an "authorization token" is provided.

Run ngrok.exe as administrator
execute the command
```
ngrok config add-authtoken <YOUR_TOKEN>
```

now execute
```
ngrok.exe http 3000
```

It will provide a public forwarding url. Copy this url.
The website is now publically hosted on the internet.

Now, open the specified folder "QRVersion11" in Android Studio.
open the class file "MyUrl.kt"
replace the url with the one that you copied from ngrok.exe

Now, to run the Android App on your smartphone, there are mainly two ways:
-> by wireless debugging mode, (refer Google Search)
-> by exporting the apk file and transferring the file to the Android phone and installing it. (refer Google Search)




## Android Application (Kotlin)  

<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/c54646b2-7ef8-45d7-a04c-96e92b2be02b" width="40%">
<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/ef43881d-f9d7-494c-9198-0b73d69ad405" width="40%">
&nbsp;

<img src="https://github.com/sarangs722/sarangs722/assets/51827238/0a9e1456-2dc9-4015-a0fe-a71930cadfbc" width="40%">
<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/46fb421c-f322-435b-a4e9-16fcc2f65a0f" width="40%">
&nbsp;

<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/b375d442-8b8b-47be-8f68-1bbd7f7c5382" width="40%">
<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/d415cd57-7d7c-4d96-8fed-4f9607bfed56" width="40%">
&nbsp;

<img src="https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/b12fae8c-693a-4c94-b875-473f13dac071" width="40%">
  
<!-- ![LoginActivity](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/c54646b2-7ef8-45d7-a04c-96e92b2be02b | width=100) -->

<!-- ![RegisterActivity](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/ef43881d-f9d7-494c-9198-0b73d69ad405) -->

<!-- ![UserHomeActivity](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/7232ed85-4d6b-4433-9db3-280512712216) -->

<!-- ![FaceRegister](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/46fb421c-f322-435b-a4e9-16fcc2f65a0f) -->

<!-- ![FaceRecognized](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/b375d442-8b8b-47be-8f68-1bbd7f7c5382)

![QRScanActivity](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/d415cd57-7d7c-4d96-8fed-4f9607bfed56) -->

<!-- ![AttendanceRecords](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/b12fae8c-693a-4c94-b875-473f13dac071) -->


## Web Portal (Node.js)

![TeacherLogin](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/9e98923c-96d1-4766-8c0d-83f146c62eff)
&nbsp;
![TeacherPortal](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/6b37383a-fa01-47ff-8326-147ef72201bd)
&nbsp;
![ViewRecords](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/d33de4b8-f53b-4ce1-9b68-af8c05233fc8)
&nbsp;
![GenerateQRCode](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/f440cf6d-23db-43a6-817d-6592bf8908f6)
&nbsp;
![QRCode](https://github.com/sarangs722/QRCode-FacialRecog-Attendance-System/assets/51827238/eb222f8c-2d9d-4746-b3c3-23f4ed348ea8)



