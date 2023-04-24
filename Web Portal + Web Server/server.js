const mongodb = require('mongodb');
const ObjectID = mongodb.ObjectId;
const crypto = require('crypto');
const express = require('express');
const bodyParser = require('body-parser');
const QRCode = require('qrcode');
const { setInterval } = require('timers');
const PORT = process.env.PORT || 3000;

const genRandomString = function(length) {
    return crypto.randomBytes(Math.ceil(length/2))
        .toString('hex')
        .slice(0, length);
}

const sha512 = function(password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt: salt,
        passwordHash: value
    };
}

var currValidQRData = {
    course: 'KAS101',
    lecdate: '2023-04-16',
    lectime: '0850',
    salt: makeRandomString(),
}

function saltHashPassword(userPassword) {
    var salt = genRandomString(16);
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt) {
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function makeRandomString() {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let counter = 0;
    while (counter < 10) {
      result += characters.charAt(Math.floor(Math.random() * 10));
      counter += 1;
    }
    return result;
}

function generateQRCodes(courseLec, lecdate, lectime) {
    // YYYY-MM-DD
    let data = {
        course: courseLec,
        date: lecdate,
        timeslot: lectime,
        salt: makeRandomString(),
    }

    currValidQRData = data;

    let stringdata = JSON.stringify(data);

    // QR Code saved as image (png)

    QRCode.toFile("qr.png", stringdata, (err) => {
        if (err) return console.log("error occured!");
    });

    
}

//Create express service
const app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));


app.use(express.static('public'));

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

app.get('/teacherLogin', (req, res) => {
    res.sendFile(__dirname + '/teacherlogin.html');
});

app.get('/adminLogin', (req, res) => {
    res.sendFile(__dirname + '/adminlogin.html');
});

//create mongodb client
const MongoClient = mongodb.MongoClient;

//connection url
const url = 'mongodb://0.0.0.0:27017';
// const url = process.env.MONGO_URI
//'mongodb://127.0.0.1:27017'
const databaseName = "qrappfirst1"

MongoClient.connect(url, {useNewUrlParser: true}, function(err, client){
    if (err)
        console.log("unable to connect to the server", err);
    else {

        // db.createCollection(databaseName, {
        //     validator: {
        //       $jsonSchema: {
        //         bsonType: "object",
        //         required: ["name", "email", "password", "salt"],
        //         additionalProperties: false,
        //         properties: {
        //           _id: {},
        //           name: {
        //             bsonType: "string",
        //             description: "'name' is required and is a string"
        //           },
        //           email: {
        //             bsonType: "string",
        //           },
        //           password: {
        //             bsonType: "string",
        //           },
        //           salt: {
        //             bsonType: "string",
        //           },
        //         }
        //       }
        //     }
        //   });

        
        // Admin Authentication
        app.post('/adminLog', (request, response, next) => {
            var post_data = request.body;

            var email = post_data.email;
            var password = post_data.password;

            if ((email.localeCompare("admin@qr.com") == 0) && (password.localeCompare("admin") == 0)) {
                console.log('Admin Logged In!');
                response.sendFile(__dirname + '/adminportal.html');
            }
            else {
                console.log('Wrong email or password for ADMIN!');
            }
        });

        // ADDING TEACHERS (from Admin Login)
        app.post('/addTeacher', (request, response, next) => {
            var post_data = request.body;

            var name = post_data.name;
            var email = post_data.email;
            var password = post_data.password;

            var insertJson = {
                'email': email,
                'password':password,
                'name':name
            };
            var db = client.db(databaseName);

            //check exists email
            db.collection('teacher')
                .find({'email':email}).count(function(err, number) {
                    if (number != 0) {
                        response.json('Email already exists');
                        console.log('Email already exists');
                    }
                    else {
                        //insert data
                        db.collection('teacher')
                            .insertOne(insertJson, function(error, res){
                                console.log('teacher added successfully');
                                response.sendFile(__dirname + "/success.html");
                            })
                    }
                })
        })

        // Teacher's login authentication check
        app.post('/teacherLog', (request, response, next) => {
            var post_data = request.body;

            var email = post_data.email;
            var password = post_data.password;

            var db = client.db(databaseName);

            db.collection('teacher')
                .find({'email':email}).count(function(err, number) {
                    if (number == 0) {
                        console.log('Email does not exists');
                    }
                    else {
                        //insert data
                        db.collection('teacher')
                            .findOne({'email':email}, function(err, user) {
                                if (password == user.password) {
                                    // response.send({email : 'email'});
                                    response.sendFile(__dirname + "/teacherportal.html");
                                    console.log('Teacher Login success');
                                }
                                else {
                                    console.log('Wrong password');
                                }
                            })
                    }
                });
        });

        // Generating QR Code
        app.post('/lectureDetails', (request, response, next) => {
            var post_data = request.body;

            // startGenerating(response, post_data.course, post_data.lecdate, post_data.lectime);

            generateQRCodes(post_data.course, post_data.lecdate, post_data.lectime);
        
            // setInterval(generateQRCodes(post_data.course, post_data.lecdate, post_data.lectime), 5000);
            // setInterval((generateQRCodes(post_data.course, post_data.lecdate, post_data.lectime)) => {response.sendFile(__dirname + '/qr.png');}, 10000);

            // // YYYY-MM-DD
            // let data = {
            //     course: post_data.course,
            //     date: post_data.lecdate,
            //     timeslot: post_data.lectime,
            //     salt: makeRandomString(),
            // }

            // let stringdata = JSON.stringify(data);

            // // QR Code saved as image (png)

            // QRCode.toFile("qr.png", stringdata, (err) => {
            //     if (err) return console.log("error occured!");
            // });


            // QR code in terminal

            // QRCode.toString(stringdata, {type: 'terminal'}, (err, QRCode) => {
            //     if (err) return console.log("error occurred!")
            //     console.log(QRCode)
            // });

            response.sendFile(__dirname + '/qr.png');

            // QR Code in base64 in terminal

            // QRCode.toDataURL(stringdata, function(err, url) {
            //     if (err) return console.log("error occurred!")
            //     console.log(url)
            // });

        });

        

        // ANDROID APP STUFF

        // QRCode + Student details sent back to server from app
        app.post('/qrStudentApp', (request, response, next) => {
            var post_data = request.body;

            // console.log(currValidQRData);
            // console.log(post_data.qrData);

            const userRoll = post_data.email;
            console.log(`trying to mark attendace of ${userRoll}...`);

            const data = post_data.qrData;
            const qrAppData = JSON.parse(data);

            // var qrAppData = JSON.parse(post_data.qrData);


            var appCourse = qrAppData.course;
            var appLecDate = qrAppData.date;
            var appLecTime = qrAppData.timeslot;
            var appSalt = qrAppData.salt;

            // console.log("course" + ": " + currValidQRData.course + " " + qrAppData.course);
            // console.log("date" + ": " + currValidQRData.date + " " + qrAppData.date);
            // console.log("timeslot" + ": " + currValidQRData.timeslot + " " + qrAppData.timeslot);
            // console.log("salt" + ": " + currValidQRData.salt + " " + qrAppData.salt);

            if ((appCourse.localeCompare(currValidQRData.course) == 0) &&
                (appLecDate.localeCompare(currValidQRData.date) == 0) &&
                (appLecTime.localeCompare(currValidQRData.timeslot) == 0) &&
                (appSalt.localeCompare(currValidQRData.salt) == 0)
            ) {
                // response.send('Attendance marked');
                // console.log('Attendance marked');
                // DB addition

                const secret = userRoll + appCourse + appLecDate + appLecTime;
                console.log(secret);

                // const hashVal = crypto.createHash('sha256', secret).digest('hex');



                var insertJson = {
                    'roll': userRoll,
                    'course': appCourse,
                    'date': appLecDate,
                    'timeslot': appLecTime,
                    'hash' : secret,
                };

                
                var db = client.db(databaseName);
    
                //check exists email
                db.collection('attendance')
                    .find({'hash':secret}).count(function(err, number) {
                        if (number != 0) {
                            response.json('Attendance already marked');
                            console.log('Attendance already marked');
                        }
                        else {
                            //insert data
                            db.collection('attendance')
                                .insertOne(insertJson, function(error, res){
                                    console.log('Attendance marked successfully');
                                    response.json('Attendance success');
                                })
                        }
                    })
                
                
            }
            else {
                response.send('Error');
                console.log('Error in scanning qr code');
            }
        });

        // Attendance Records request from Android Application
        app.get("/attendanceRecords", (req, res) => {
            var db = client.db(databaseName);
            db.collection('attendance').find({}).toArray(function(err, result) {
                if (err) throw err;
                console.log(result);
                res.send(result);
            })
        })

        //Register
        app.post('/register', (request, response, next)=> {
            var post_data = request.body;

            var plain_password = post_data.password;
            var hash_data = saltHashPassword(plain_password);

            var password = hash_data.passwordHash;
            var salt = hash_data.salt;

            var name = post_data.name;
            var email = post_data.email;

            var insertJson = {
                'email': email,
                'password':password,
                'salt':salt, 
                'name':name
            };
            var db = client.db(databaseName);

            //check exists email
            db.collection('user')
                .find({'email':email}).count(function(err, number) {
                    if (number != 0) {
                        response.json('Email already exists');
                        console.log('Email already exists');
                    }
                    else {
                        //insert data
                        db.collection('user')
                            .insertOne(insertJson, function(error, res){
                                response.json('Registration success');
                                console.log('Student Registration success');
                            })
                    }
                })
        });

        app.post('/login', (request, response, next)=> {
            var post_data = request.body;

            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db(databaseName);

            //check exists email
            db.collection('user')
                .find({'email':email}).count(function(err, number) {
                    if (number == 0) {
                        response.json('Email not exists');
                        console.log('Email not exists');
                    }
                    else {
                        //insert data
                        db.collection('user')
                            .findOne({'email':email}, function(err, user) {
                                var salt = user.salt;
                                var hashed_password = checkHashPassword(userPassword, salt).passwordHash;
                                var encrypted_password = user.password;
                                if (hashed_password == encrypted_password) {
                                    response.json('Login success');
                                    console.log('Student Login success');
                                }
                                else {
                                    response.json('Wrong password');
                                    console.log('Student Wrong password');
                                }
                            })
                    }
                })
        });

        //start web server
        app.listen(PORT, () => {
            console.log(`connected to mongodb server, webservice running on port ${PORT}`);
        })
    }
})