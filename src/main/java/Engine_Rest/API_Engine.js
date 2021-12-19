

const express = require("express");
const app = express();
const port = 3000;

const bodyParse = require('body-parser');
const mongoose = require('mongoose');
const mongojs = require('mongojs');
const URL_DB = "mongodb://localhost:27017/parque";
var db = mongojs(URL_DB);
var id = mongojs.ObjectI;

app.use(bodyParse.json());

app.get("/", (req, res) => {
    res.json({message: 'Pagina de inicio de aplicacion ejemplo SD'})

});

app.listen(port, () => {
    console.log(`Ejecutando la aplicacion api rest en ${port}`);
});

app.get('/api', (req, res, next) =>{
    console.log('GET /api');
    console.log(req.collection);

    db.getCollectionNames((err, colecciones) => {
        if(err) return next(err);

        console.log(colecciones);
        res.json({
            result: 'ok',
            colecciones: colecciones
        });
    });
});

app.get('/api/mapa', (req, res, next) =>{
    mongoose.connect('mongodb://localhost:27017/parque');

    var dbb = mongoose.connection;

    if(!dbb)
        console.log("error");
    else
        console.log("bieeeen")

    db.mapa.find((err, colecciones) => {
        if(err) return next(err);

        console.log(colecciones);
        res.json({
            result: 'ok',
            colecciones: colecciones
        });
    });
});

app.post('/api/probando', (req, res) => {
    mongoose.connect('mongodb://localhost:27017/parque');

    const nuevoElemento = req.body;
    console.log(req.body);

    var dbb = mongoose.connection;

    if (!dbb)
        console.log("error");
    else
        console.log("bieeeen")

    //var jsonTexto = '{"color":"blancoo","nombre":"aaa"}';
    /*
    var coche = JSON.parse(nuevoElemento);
    console.log(coche);
     */

    db.probando.save(nuevoElemento, (err, elementoGuardado) =>{
        if(err) return next(err);

        console.log(nuevoElemento);
        res.status(200).json({
            result: 'ok'
        });
    });


});

