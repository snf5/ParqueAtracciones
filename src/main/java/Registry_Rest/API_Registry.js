/*
const bbdd = "mongodb://localhost:27017/parque";
const express = require("express");
const app = express();
const mongoose = require("mongoose");
const mongojs = require("mongojs");
const bodyParse = require("body-parser");
const logger = require("morgan");
const port = 3023;
const https = require('https');
const fs = require('fs');
const md5 = require("blueimp-md5");


let db = mongojs(bbdd);
let id = mongojs.ObjectID;

const options = {
    key: fs.readFileSync('cert/key.pem'),
    cert: fs.readFileSync('cert/cert.pem')
};

app.use(logger('dev'));
app.use(express.json());
app.use(bodyParse.json());
app.use(express.urlencoded({extended: false}))

app.post('/api/registry', (req, res, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Registrar usuario');
        console.log('---------------------------------');
        let {alias, nombre, contrasenya} = req.body;
        let {ip} = req.body
        let fechan = new Date();
        let fech = (fechan.getDay()+12) + "/" + (fechan.getMonth()+1) + "/" + fechan.getFullYear();
        let hash = md5(contrasenya);


        if(!alias || !nombre || !contrasenya || !ip) {
            res.status(400).json( {
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({alias: alias}, (err, userTwo) => {
                if(err) {
                    res.status(500).json({
                        error: 'Database',
                        description: 'Error en la base de datos'
                    });
                } else if(userTwo != null) {
                    res.status(600).json({
                        error: 'User already exits',
                        description: 'El usuario ya existe',
                        userTwo: userTwo
                    });
                } else {
                    db.usuario.insert(
                        {alias: alias, nombre: nombre, contrasenya: hash},
                        (err, newUser) => {
                            console.log(newUser);
                            res.status(700).json( {
                                result: 'Usuario registrado correctamente',
                                userTwo: newUser
                        });
                    });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Alta", descripcion: "Registro de " + alias + "."},
                        (err, anotherUser) => {
                            if(err) {
                                console.log("Fichero no actualizado.");
                            } else {
                                console.log("Fichero actualizado");
                            }
                    });
                    let message = "El usuario " + ip + " ha creado su cuenta. Fecha: " + fech + "." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                }
            });
        }
    }
});

app.put('/api/registry/:id', (request, response, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Modificar usuario');
        console.log('---------------------------------');

        let ident = request.params.id;
        let {alias, nombre, contrasenya} = request.body;
        let {ip} = request.body;
        let fechan = new Date();
        let fech = (fechan.getDay()+12) + "/" + (fechan.getMonth()+1) + "/" + fechan.getFullYear();
        if( !alias || !nombre || !contrasenya || !ip) {
            response.json({
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({_id: id(ident)}, (err, userTwo) => {
                if(err) return next(err);
                if(userTwo == null) {
                    response.json({
                        error: "Wrong id",
                       result: "El usuario introducido no existe."
                    });
                } else {
                    db.usuario.update(
                        { _id: id(ident) },
                        {$set: {alias: alias, nombre: nombre, contrasenya: contrasenya}},
                        {safe: true, multi: false},
                        (err, result) => {
                            if(err) return next(err);
                            console.log(result);
                            response.json({
                                result: "Usuario modificado correctamente",
                                name: alias
                            });
                        });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Actualización", descripcion: "Actualización de " + alias + "."},
                        (err, anotherUser) => {
                            if(err) return next(err)
                            console.log("Fichero actualizado.");
                        });
                    let message = "El usuario " + ip + " ha creado su cuenta. Fecha: " + fech + "." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                }
            });
        }
    }
});

app.delete('api/registry/:id', (req, res, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Borrar usuario');
        console.log('---------------------------------');
        let ident = req.params.id;
        let {alias, nombre, contrasenya} = req.body;
        let {ip} = req.body;
        let fechan = new Date();
        let fech = (fechan.getDay()+12) + "/" + (fechan.getMonth()+1) + "/" + fechan.getFullYear();

        if(!ident || !alias || !nombre || !contrasenya || !ip) {
            res.json({
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({_id: id(ident)}, (err, userTwo) => {
                if(err) return next(err);
                if(userTwo == null) {
                    response.json({
                        error: "Wrong id",
                        result: "El usuario introducido no existe."
                    });
                } else {
                    db.usuario.remove(
                        { _id: id(ident) },
                        (err, resultado) => {
                            if(err) return next(err);
                            console.log(resultado);
                            res.json({
                                result: "Usuario eliminado correctamente",
                                identification: ident
                            });
                        });
                    db.historial.save(
                        {fecha: fech, usuario: ip, accion: "Actualización", descripcion: "Actualización de " + alias + "."},
                        (err, anotherUser) => {
                            if(err) return next(err);
                            console.log("Fichero actualizado");
                        });
                    let message = "El usuario " + ip + " ha creado su cuenta. Fecha: " + fech + "." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado.");
                    });
                }
            });
        }
    }
});


https.createServer(options, app).listen(port, () => {
    console.log(`API-REGISTER ejecutándose en https://localhost:${port}/api/registry`);
});



app.get('/', function (req, res){
    res.send('holaaaaaaaa');
    console.log('yeeee');
});

https.createServer({
    cert: fs.readFileSync('cert.pem'),
    key: fs.readFileSync('key.pem')
}, app).listen(port, function(){
    console.log('yeeee');
});





app.listen(port, () => {
    console.log(`Ejecutando la aplicacion api rest en ${port}`);
});

 */

const bbdd = "mongodb://localhost:27017/parque";
const express = require("express");
const app = express();
const mongoose = require("mongoose");
const mongojs = require("mongojs");
const bodyParse = require("body-parser");
const logger = require("morgan");
const port = 3032;
const https = require("https");
const fs = require("fs");
const md5 = require("blueimp-md5");
let os = require('os');
const journey = require('journey');

//var mrouter = new (journey.Router)();

let interfaces = os.networkInterfaces();
let addresses = [];
for (let k in interfaces) {
    for (let k2 in interfaces[k]) {
        let address = interfaces[k][k2];
        if (address.family === 'IPv4' && !address.internal) {
            addresses.push(address.address);
        }
    }
}

let db = mongojs(bbdd);
let id = mongojs.ObjectID;

const options = {
    key: fs.readFileSync('C:/cert/key.pem'),
    cert: fs.readFileSync('C:/cert/cert.pem')
};


app.use(logger('dev'));
app.use(express.json());
app.use(bodyParse.json());

let fechan = new Date();
let fechas = fechan.toLocaleString();
let fech = fechas.substr(0,10);

app.post('/api/registry/', (req, res, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Registrar usuario');
        console.log('---------------------------------');
        let {alias, nombre, contrasenya} = req.body;
        let {ip} = req.body;
        //let hash = md5(contrasenya);



        if(!alias || !nombre || !contrasenya || !ip) {
            res.status(400).json( {
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({alias: alias}, (err, userTwo) => {
                if(err) {
                    return next(err);
                } else if(userTwo != null) {
                    res.status(600).json({
                        error: 'User already exits',
                        description: 'El usuario ya existe',
                        userTwo: userTwo
                    });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Error", descripcion: "El usuario " + alias + " no existe, error al intentar registrarse."},
                        (err, anotherUser) => {
                            if(err) return next(err)
                            console.log("Fichero actualizado.");
                        });
                    let message = "Usuario: " + alias + " / Ip:" + ip + " / Acción: Error / Fecha: " + fech + " / Descripción: El usuario " + alias + " no existe, error al intentar registrarse." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                } else {
                    db.usuario.insert(
                        {alias: alias, nombre: nombre, contrasenya: contrasenya},
                        (err, newUser) => {
                            console.log(newUser);
                            res.status(700).json( {
                                result: 'Usuario registrado correctamente',
                                userTwo: newUser
                            });
                        });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Alta", descripcion: "Registro de " + alias + "."},
                        (err, anotherUser) => {
                            if(err) {
                                console.log("Fichero no actualizado.");
                            } else {
                                console.log("Fichero actualizado");
                            }
                        });
                    let message = "Usuario: " + alias + " / Ip:" + ip + " / Acción: Alta / Fecha: " + fech + " / Descripción: El usuario " + alias + " se ha registrado." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                }
            });
        }
    }
});

app.put('/api/registry/:id', (request, response, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Modificar usuario');
        console.log('---------------------------------');

        let ident = request.params.id;
        let {nombre, contrasenya} = request.body;
        let {ip} = request.body;
        let hash = md5(contrasenya);

        if(!ident || !nombre || !contrasenya || !ip) {
            response.json({
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({alias: ident}, (err, userTwo) => {
                if(err) return next(err);
                if(userTwo == null) {
                    response.json({
                        error: "Wrong id",
                        result: "El usuario introducido no existe."
                    });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Error", descripcion: "El usuario " + ident + " no existe, error al intentar modificarlo."},
                        (err, anotherUser) => {
                            if(err) return next(err)
                            console.log("Fichero actualizado.");
                        });
                    let message = "Usuario: " + ident + " / Ip:" + ip + " / Acción: Error / Fecha: " + fech + " / Descripción: El usuario " + ident + " no existe, error al intentar modificarlo." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                } else {
                    db.usuario.update(
                        { alias: ident },
                        {$set: {nombre: nombre, contrasenya: hash}},
                        {safe: true, multi: false},
                        (err, result) => {
                            if(err) return next(err);
                            console.log(result);
                            response.json({
                                result: "Usuario modificado correctamente",
                                name: ident
                            });
                        });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Actualización", descripcion: "Actualización de " + ident + "."},
                        (err, anotherUser) => {
                            if(err) return next(err)
                            console.log("Fichero actualizado.");
                        });
                    let message = "Usuario: " + ident + " / Ip:" + ip + " / Acción: Actualización / Fecha: " + fech + " / Descripción: El usuario " + ident + " ha modificado su perfil." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                }
            });
        }
    }
});

app.delete('/api/registry/:id', (req, res, next) => {
    mongoose.connect('mongodb://localhost:27017/parque');
    if(!mongoose.connection) {
        console.log('Error de conexión a la BBDD');
    } else {
        console.log('---------------------------------');
        console.log('Conectado con éxito a la BBDD');
        console.log('---------------------------------');
        console.log('Borrar usuario');
        console.log('---------------------------------');
        let ident = req.params.id;
        let {nombre, contrasenya} = req.body;
        let {ip} = req.body;
        let hash = md5(contrasenya);

        if(!ident || !nombre || !contrasenya || !ip) {
            res.json({
                error: 'Wrong data',
                description: 'Información proporcionada incorrecta'
            });
        } else {
            db.usuario.findOne({alias: ident}, (err, userTwo) => {
                if(err) return next(err);
                if(userTwo == null) {
                    res.json({
                        error: "Wrong id",
                        result: "El usuario introducido no existe."
                    });
                    db.historial.insert(
                        {fecha: fech, usuario: ip, accion: "Error", descripcion: "El usuario " + ident + " no existe, error al intentar borrarlo."},
                        (err, anotherUser) => {
                            if(err) return next(err)
                            console.log("Fichero actualizado.");
                        });
                    let message = "Usuario: " + ident + " / Ip:" + ip + " / Acción: Error / Fecha: " + fech + " / Descripción: El usuario " + ident + " no existe, error al intentar borralo." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado");
                    });
                } else {
                    db.usuario.remove(
                        { alias: ident },
                        (err, resultado) => {
                            if(err) return next(err);
                            console.log(resultado);
                            res.json({
                                result: "Usuario eliminado correctamente",
                                name: ident
                            });
                        });
                    db.historial.save(
                        {fecha: fech, usuario: ip, accion: "Baja", descripcion: "Baja de " + ident + "."},
                        (err, anotherUser) => {
                            if(err) return next(err);
                            console.log("Fichero actualizado");
                        });
                    let message = "Usuario: " + ident + " / Ip:" + ip + " / Acción: Baja / Fecha: " + fech + " / Descripción: El usuario " + ident + " ha eliminado su usuario." + "\n";
                    fs.appendFile("history.txt", message, (err) => {
                        if(err) throw err;
                        console.log("Fichero txt actualizado.");
                    });
                }
            });
        }
    }
});

/*
mrouter.map(function () {
    this.root.bind(function (req, res) {
        res.send("Bienvenido al API REST Registry")
    });

    this.post().bind(function (req, res, next) {
        mongoose.connect('mongodb://localhost:27017/parque');
        if(!mongoose.connection) {
            console.log('Error de conexión a la BBDD');
        } else {
            console.log('---------------------------------');
            console.log('Conectado con éxito a la BBDD');
            console.log('---------------------------------');
            console.log('Registrar usuario');
            console.log('---------------------------------');
            let {alias, nombre, contrasenya} = req.body;
            let {ip} = req.body
            let fechan = new Date();
            let fech = (fechan.getDay()+12) + "/" + (fechan.getMonth()+1) + "/" + fechan.getFullYear();

            if(!alias || !nombre || !contrasenya || !ip) {
                res.status(400).json( {
                    error: 'Wrong data',
                    description: 'Información proporcionada incorrecta'
                });
            } else {
                db.usuario.findOne({alias: alias}, (err, userTwo) => {
                    if(err) {
                        res.status(500).json({
                            error: 'Database',
                            description: 'Error en la base de datos'
                        });
                    } else if(userTwo != null) {
                        res.status(600).json({
                            error: 'User already exits',
                            description: 'El usuario ya existe',
                            userTwo: userTwo
                        });
                    } else {
                        db.usuario.insert(
                            {alias: alias, nombre: nombre, contrasenya: contrasenya},
                            (err, newUser) => {
                                console.log(newUser);
                                res.status(700).json( {
                                    result: 'Usuario registrado correctamente',
                                    userTwo: newUser
                                });
                            });
                        db.historial.insert(
                            {fecha: fech, usuario: ip, accion: "Alta", descripcion: "Registro de " + alias + "."},
                            (err, anotherUser) => {
                                if(err) {
                                    console.log("Fichero no actualizado.");
                                } else {
                                    console.log("Fichero actualizado");
                                }
                            });
                        let message = "El usuario " + ip + " ha creado su cuenta. Fecha: " + fech + "." + "\n";
                        fs.appendFile("history.txt", message, (err) => {
                            if(err) throw err;
                            console.log("Fichero txt actualizado");
                        });
                    }
                });
            }
        }
    });


    this.put(/^param1\/([A-Za-z0-9_]+)$/).bind(function (req, res, param1, next) {
        mongoose.connect('mongodb://localhost:27017/parque');
        if(!mongoose.connection) {
            console.log('Error de conexión a la BBDD');
        } else {
            console.log('---------------------------------');
            console.log('Conectado con éxito a la BBDD');
            console.log('---------------------------------');
            console.log('Modificar usuario');
            console.log('---------------------------------');

            let ident = param1;
            let {alias, nombre, contrasenya} = req.body;
            let {ip} = req.body;
            let fechan = new Date();
            let fech = (fechan.getDay()+12) + "/" + (fechan.getMonth()+1) + "/" + fechan.getFullYear();
            if(!ident || !alias || !nombre || !contrasenya || !ip) {
                res.json({
                    error: 'Wrong data',
                    description: 'Información proporcionada incorrecta'
                });
            } else {
                db.usuario.findOne({_id: id(ident)}, (err, userTwo) => {
                    if(err) return next(err);
                    if(userTwo == null) {
                        res.json({
                            error: "Wrong id",
                            result: "El usuario introducido no existe."
                        });
                    } else {
                        db.usuario.update(
                            { _id: id(ident) },
                            {$set: {alias: alias, nombre: nombre, contrasenya: contrasenya}},
                            {safe: true, multi: false},
                            (err, result) => {
                                if(err) return next(err);
                                console.log(result);
                                response.json({
                                    result: "Usuario modificado correctamente",
                                    name: alias
                                });
                            });
                        db.historial.insert(
                            {fecha: fech, usuario: ip, accion: "Actualización", descripcion: "Actualización de " + alias + "."},
                            (err, anotherUser) => {
                                if(err) return next(err)
                                console.log("Fichero actualizado.");
                            });
                        let message = "El usuario " + ip + " ha creado su cuenta. Fecha: " + fech + "." + "\n";
                        fs.appendFile("history.txt", message, (err) => {
                            if(err) throw err;
                            console.log("Fichero txt actualizado");
                        });
                    }
                });
            }
        }
    });
});
*/
https.createServer(options,app).listen(port, () => {
    console.log(`API-REGISTER ejecutándose en https://${addresses[1]}:${port}/api/registry`);
});

