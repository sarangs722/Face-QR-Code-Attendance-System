const mongodb = require('mongodb');
const ObjectID = mongodb.ObjectId;
const crypto = require('crypto');
const express = require('express');
const bodyParser = require('body-parser');
const QRCode = require('qrcode');
const openurl = require('openurl');
const ejs = require("ejs");
const axios = require('axios');
const { setInterval } = require('timers');
const PORT = process.env.PORT || 3000;

//Create express service
const app = express();

app.set('views', './views');
app.set('view engine', 'ejs');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));


app.use(express.static('public'));


const genRandomString = function (length) {
    return crypto.randomBytes(Math.ceil(length / 2))
        .toString('hex')
        .slice(0, length);
}

const sha512 = function (password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt: salt,
        passwordHash: value
    };
}


let currValidQRData = {
    course: 'KAS101',
    lecdate: '2023-04-16',
    lectime: '0850',
    salt: makeRandomString(),
}

let qrUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIQAAACECAYAAABRRIOnAAAAAklEQVR4AewaftIAAAOoSURBVO3BQY5biRUEwcoH3v/KaS1mUd58gCBbI9kVgb9k5h+XmXKZKZeZcpkpl5lymSmXmXKZKZeZcpkpl5lymSmXmXKZKZeZ8sqHgPxOaj4BpKn5BJCmpgH5ndR84jJTLjPlMlNe+TI13wTkCZCm5omaJ0CeqPmEmm8C8k2XmXKZKZeZ8soPA/IONe9Q04A0NU+ANDUNSAPyTUDeoeYnXWbKZaZcZsor81+ANDUNyP+yy0y5zJTLTHnlLwekqWlAmpqm5gmQ/yeXmXKZKZeZ8soPU/OT1DxR04A8UdPUPAHyCTV/kstMucyUy0x55cuA/E5AmpoGpKlpQJ4AaWo+AeRPdpkpl5lymSmvfEjNv0nNNwFpaj6h5m9ymSmXmXKZKa98CEhT04A0NQ1IU9OANDXfpKYBaUCamm8C0tQ8AdLUfNNlplxmymWm4C/5IiBNzZ8ESFPTgDQ1DUhT04C8Q00D0tQ8AdLUfOIyUy4z5TJTXvlhQJ6oeQKkqWlAPgHkCZB3qGlAmppPAPlJl5lymSmXmfLKh4A8UfMOIE3NEzVPgDQ1T4A0NQ3IEyBNTQPS1DQ1/6bLTLnMlMtMeeVDahqQT6hpQD6hpgH5hJp3AGlqPqGmAfmmy0y5zJTLTHnlhwF5h5qm5gmQb1LTgDQ1DUhT8wRIU9OANDUNyE+6zJTLTLnMlFc+BKSpeQeQBuSJmidqGpBPqGlAmponQJqadwBpahqQb7rMlMtMucwU/CVfBKSpaUCamgakqflJQN6hpgH5JjXvANLUfOIyUy4z5TJTXvkQkKamAXkCpKlpQJqaBuSJmgakqXkC5E+m5psuM+UyUy4zBX/JXwzIJ9S8A8gTNe8A0tQ0IE1NA9LUfOIyUy4z5TJTXvkQkN9JTVPTgDxR8wRIU/MJIE3NJ4D8pMtMucyUy0x55cvUfBOQbwLS1DQ1T9Q0IE/UfELN73SZKZeZcpkpr/wwIO9Q8w4gTU0D0tQ0IE3NJ4B8AkhT8ztdZsplplxmyit/OTXvAPIESFPzRM0TIE1NA9LUPAHS1HzTZaZcZsplprzylwPyTWoakH8TkKamAWlqPnGZKZeZcpkpr/wwNT9JTQPyRE0D0oC8Q80TIA3IEyBNTQPS1HzTZaZcZsplprzyZUB+JyBP1DQgTc0TIE+AvENNA9LUvANIU/OJy0y5zJTLTMFfMvOPy0y5zJTLTLnMlMtMucyUy0y5zJTLTLnMlMtMucyUy0y5zJTLTPkPVxueLq/I6J0AAAAASUVORK5CYII=";

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

// function generateQRCodes(courseLec, lecdate, lectime) {
//     // YYYY-MM-DD
//     let data = {
//         course: courseLec,
//         date: lecdate,
//         timeslot: lectime,
//     };

//     for (let i = 0; i < 30; i++) {
//         data.salt = makeRandomString();
//         qrCodeList.push({ ...data });
//     }

//     currValidQRData = qrCodeList[0];
// }

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

    // QRCode.toFile("./qrCodes/qr.png", stringdata, (err) => {
    //     if (err) return console.log("error occured!");
    // });

    QRCode.toDataURL(stringdata, function (err, url) {
        console.log('QR generated!');
        qrUrl = url;
    });
}

let redirectPageQR = true;

function keepGeneratingQR() {
    let newQRData = { ...currValidQRData };
    newQRData.salt = makeRandomString();
    currValidQRData = newQRData;
    let stringdata = JSON.stringify(newQRData);

    // QRCode.toFile("qr.png", stringdata, (err) => {
    //     if (err) return console.log("error occured in qr image saving!");
    // });

    QRCode.toDataURL(stringdata, function (err, url) {
        console.log('QR generated!');
        qrUrl = url;
    });

    if (!redirectPageQR) return;

    openurl.open('http://localhost:3000/qrOutput');

    // boolQRPage = !boolQRPage;
    // setTimeout(() => axios.get(`http://localhost:3000/qrOutput${boolQRPage ? 1 : 2}`), 7000);

    // axios.get("https://64f4-103-208-68-235.ngrok-free.app/qrOutput").then(response => {
    //     console.log('get again');
    // }).catch(error => console.log('Error to fetch page'));

    // setTimeout(() =>
    //     axios.get('https://64f4-103-208-68-235.ngrok-free.app/qrOutput'), 7000);
}


app.get('/', (req, res) => {
    // redirectPageQR = true;
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

MongoClient.connect(url, { useNewUrlParser: true }, function (err, client) {
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
                'password': password,
                'name': name
            };
            var db = client.db(databaseName);

            //check exists email
            db.collection('teacher')
                .find({ 'email': email }).count(function (err, number) {
                    if (number != 0) {
                        response.json('Email already exists');
                        console.log('Email already exists');
                    }
                    else {
                        //insert data
                        db.collection('teacher')
                            .insertOne(insertJson, function (error, res) {
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
                .find({ 'email': email }).count(function (err, number) {
                    if (number == 0) {
                        console.log('Email does not exists');
                    }
                    else {
                        //insert data
                        db.collection('teacher')
                            .findOne({ 'email': email }, function (err, user) {
                                if (password == user.password) {
                                    // response.send({email : 'email'});

                                    response.redirect('/teacherPortal');
                                    // response.sendFile(__dirname + "/teacherportal.html");
                                    console.log('Teacher Login success');
                                }
                                else {
                                    console.log('Wrong password');
                                }
                            })
                    }
                });
        });

        app.get('/adminPortal', (req, res) => {
            res.sendFile(__dirname + '/adminportal.html');
        })

        // Outputting to teacher portal page
        app.get('/teacherPortal', (req, res) => {
            res.sendFile(__dirname + "/teacherportal.html");
        });

        app.get('/teacherRecords', (req, res) => {
            res.render('teacherrecords', { records: [] });
        });

        app.get('/teacherGenerateQr', (req, res) => {
            res.sendFile(__dirname + "/teachergenerateqr.html");
        })

        app.post('/backToTeacher', (req, res) => {
            redirectPageQR = false;
            // res.sendFile(__dirname + "/teacherportal.html");
            res.redirect('/teacherPortal');
        })

        app.get('/qrOutput', (req, res) => {
            res.render('qrOutput', { qrUrl: qrUrl });
            setTimeout(() => keepGeneratingQR(), 10000);

            // res.sendFile(__dirname + '/qr.png');
            // keepGeneratingQR();
            // setTimeout(keepGeneratingQR, 7000);

            // keepGeneratingQR();

            // res.render("qrOutput", { qrCodeList: qrCodeList });

            // res.sendFile(__dirname + '/qr.png');
            // console.log("first qr image rendered");

            // let newQRData = { ...currValidQRData };


            // setInterval(() => {
            //     window.location = self.location.href;
            // }, 7000);
            // res.redirect("/qrOutput");
        });

        // Generating QR Code
        app.post('/lectureDetails', (request, response, next) => {
            const post_data = request.body;

            // startGenerating(response, post_data.course, post_data.lecdate, post_data.lectime);
            redirectPageQR = true;
            generateQRCodes(post_data.course, post_data.lecdate, post_data.lectime);

            setTimeout(() => response.redirect("/qrOutput"), 3000);
            //maybe async await on down func


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




            // THIS WAS THE LINE EARLIER
            // response.sendFile(__dirname + '/qr.png');





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
                    'hash': secret,
                };


                var db = client.db(databaseName);

                //check exists email
                db.collection('attendance')
                    .find({ 'hash': secret }).count(function (err, number) {
                        if (number != 0) {
                            response.json('Attendance already marked');
                            console.log('Attendance already marked');
                        }
                        else {
                            //insert data

                            // Including user details
                            console.log(userRoll);
                            axios.get(`http://localhost:3000/userDetails/${userRoll}`)
                                .then(resp => {
                                    console.log("resp", ...resp.data)
                                    insertJson.details = { ...resp.data };
                                    db.collection('attendance')
                                        .insertOne(insertJson, function (error, res) {
                                            console.log('Attendance marked successfully');
                                            response.json('Attendance success');
                                        })

                                }).catch(error => console.log('Error to fetch page'));



                            // db.collection('attendance')
                            //     .insertOne(insertJson, function (error, res) {
                            //         console.log('Attendance marked successfully');
                            //         response.json('Attendance success');
                            //     })
                        }
                    })


            }
            else {
                response.send('invalid');
                console.log('Invalid scanning of qr code.');
            }
        });

        app.post("/recordsQuery", (req, res) => {
            const post_data = req.body;
            const db = client.db(databaseName);

            db.collection('attendance').find({ course: post_data.course, date: post_data.lecdate, timeslot: post_data.lectime }).toArray(function (err, result) {
                if (err) throw err;
                console.log(result);
                res.render('teacherrecords', { records: result });
                // res.send(result);
            });


            // res.redirect('/teacherRecords');
            // maybe need to add below code when attendance data is being inserted to mongodb database
            // db.collection("attendance").aggregate([{
            //     $lookup: {
            //         from: "user",
            //         localField: "email",
            //         foreignField: "roll",
            //         as: "copies_sold"
            //     }
            // }]);

            // async function getStudents() {
            //     return await db.collection("attendance").find({ course: post_data.course, date: post_data.lecdate, timeslot: post_data.lectime }, { projection: { roll: 1, _id: 0 } }).toArray()
            // };

            // async function getStudentData() {
            //     return await db.collection("user").find({email:roll})
            // }

            // getStudents().then(studentList => {
            //     console.log(studentList);

            //     // for (student of studentList) {
            //     //     let stData = app.get(`/userDetails/${student.roll}`)
            //     //     console.log(stData);
            //     //     // studentData.push(stData);
            //     // }
            // });

            // console.log("bahare: ", studList);


            // db.collection('attendance').find({ course: post_data.course, date: post_data.lecdate, timeslot: post_data.lectime }).toArray(function (err, result) {
            //     if (err) throw err;
            //     // console.log(result);
            //     studentList = result.json
            //     res.send(result);
            // });

            // console.log(studentList);
            // for (student of studentList) {
            //     console.log(student);
            //     studentData.add(app.get(`/userDetails/${student.roll}`));
            // }
            // res.send(studentData);

            // res.redirect('/teacherRecords');
        });

        // ALL Attendance Records request from Android Application
        app.get("/attendanceRecords", (req, res) => {
            const db = client.db(databaseName);
            db.collection('attendance').find({}).toArray(function (err, result) {
                if (err) throw err;
                console.log(result);
                res.send(result);
            })
        })

        app.get("/attendanceRecords/:roll", (req, res) => {
            const db = client.db(databaseName);
            db.collection('attendance').find({ roll: req.params.roll }).toArray(function (err, result) {
                if (err) throw err;
                console.log(result);
                res.send(result);
            })
        })

        app.get("/userDetails/:roll", (req, res) => {
            const db = client.db(databaseName);
            db.collection('user').find({ email: req.params.roll }).toArray(function (err, result) {
                if (err) throw err;
                console.log(result);
                res.send(result);
            })
        })

        //Register Mobile User (Student)
        app.post('/register', (request, response, next) => {
            var post_data = request.body;

            var plain_password = post_data.password;
            var hash_data = saltHashPassword(plain_password);

            var password = hash_data.passwordHash;
            var salt = hash_data.salt;

            var name = post_data.name;
            var email = post_data.email;
            var phone = post_data.phone;

            var insertJson = {
                'email': email,
                'password': password,
                'salt': salt,
                'name': name,
                'phone': phone
            };
            var db = client.db(databaseName);

            //check exists email
            db.collection('user')
                .find({ 'email': email }).count(function (err, number) {
                    if (number != 0) {
                        response.json('Email already exists');
                        console.log('Email already exists');
                    }
                    else {
                        //insert data
                        db.collection('user')
                            .insertOne(insertJson, function (error, res) {
                                response.json('Registration success');
                                console.log('Student Registration success');
                            })
                    }
                })
        });

        // Login Mobile User (Student)
        app.post('/login', (request, response, next) => {
            var post_data = request.body;

            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db(databaseName);

            //check exists email
            db.collection('user')
                .find({ 'email': email }).count(function (err, number) {
                    if (number == 0) {
                        response.json('User does not exist');
                        console.log('User not exists');
                    }
                    else {
                        //insert data
                        db.collection('user')
                            .findOne({ 'email': email }, function (err, user) {
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