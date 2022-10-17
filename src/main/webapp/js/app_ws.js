import "https://api.mapbox.com/mapbox-gl-js/v2.8.2/mapbox-gl.js"
import "https://unpkg.com/deck.gl@^8.6.6/dist.min.js"
import "https://api.tiles.mapbox.com/mapbox-gl-js/v1.10.0/mapbox-gl.js"
import "https://unpkg.com/@deck.gl/layers@^8.6.6/dist.min.js"

const {MapboxLayer, ScatterplotLayer, ArcLayer, HeatmapLayer, GeoJsonLayer, LineLayer} = deck;


var promenadeBaseURI = '/promenade/rest/';
// var restServerLocation = 'http://localhost:8080';
// var restServerLocation = 'http://promenade-areaname-promenade-lyon.apps.kube.rcost.unisannio.it';
var restServerLocation = window.location.protocol + '//' + window.location.host;
const wsServerLocation = "ws://172.31.1.158:31700";
// const wsServerLocation = "ws://127.0.0.1:8025";
const wsEndpoint = "/ws/messages/out";

const map_div = document.getElementById("map")
const staticTrafficButton = document.getElementById("staticTrafficButton")
const startTrafficButton = document.getElementById("startTrafficButton")
const stopTrafficButton = document.getElementById("stopTrafficButton")
const heatMapStaticButton = document.getElementById("heatMapStaticButton")
const heatMapDynamicButton = document.getElementById("heatMapDynamicButton")
const heatMapSnapshotButton = document.getElementById("heatMapSnapshotButton")
const trafficLegendDiv = document.getElementById("legend-traffic")
const datePicker = document.getElementById("date-picker")
const timePicker = document.getElementById("time-picker")
const radioNorm = document.getElementsByName("radio-norm")
const overlapCheckbox = document.getElementById("overlap-box")
const cleanMapButton = document.getElementById("cleanMapButton")
const toggleAreas = document.getElementById("toggle-areas")

const TRAFFIC_LAYER_PREDECESSOR = "aerialway"
// const TRAFFIC_LAYER_PREDECESSOR = "admin-0-boundary-disputed"
const HEATMAP_LAYER_PREDECESSOR = "admin-0-boundary"
const POLYGON_LAYER_PREDECESSOR = "admin-0-boundary-disputed"
// const POLYGON_LAYER_PREDECESSOR = "aerialway"


datePicker.value = "2018-09-06"
//------------------- Global variables

const colormap = [
    [165, 0, 38, 255],
    [215, 48, 39, 255],
    [244, 109, 67, 255],
    [253, 174, 97, 255],
    [254, 224, 144, 255],
    [255, 255, 191, 255],
    [224, 243, 248, 255],
    [171, 217, 233, 255],
    [116, 173, 209, 255],
    [69, 117, 180, 255],
    [49, 54, 149, 255]
]
const noDataColor = [102, 189, 99, 255]

var socket = null;
var myHeatmapLayers = new Map();
var myTrafficLayers = new Map();
var myPolygonLayers = new Map();


//------------------- Map functions

mapboxgl.accessToken = 'pk.eyJ1IjoiY2FybWluZWNvbGFydXNzbzkzIiwiYSI6ImNsNDVreGVsdzAxd2kzam84eTk1YmRjNGYifQ.M3Jmf9qni_OKR7fwpeeNYw';
let map = new mapboxgl.Map({
    container: map_div,
    style: 'mapbox://styles/mapbox/streets-v11',
    center: [4.826683, 45.749572],
    zoom: 15
    // getTooltip: ({object}) => object && object.message
});


function calculateColor(fftt, tt) {
    let index;
    let ratio = fftt / tt;
    if (ratio >= 1) {
        index = colormap.length - 1
    } else {
        index = Math.floor(ratio * (colormap.length - 1))
    }
    // console.log(fftt + ":" + tt + ":" + index)
    return colormap[index]
}

function cleanMap(cleanHeatmap, cleanTraffic) {
    console.log(map)
    console.log(map.getStyle().layers)
    // trafficLegendDiv.setAttribute("hidden", "hidden");
    if (cleanHeatmap) {
        myHeatmapLayers.forEach(layer => {
            map.removeLayer(layer.id)
        })
        myHeatmapLayers = new Map()
        console.log("Heatmap cleaned!");
    }
    if (cleanTraffic) {
        myTrafficLayers.forEach(layer => {
            map.removeLayer(layer.id)
        })
        myTrafficLayers = new Map()
        console.log("Traffic cleaned!");
    }
}

function createStreetLayer(id, data) {
    return new MapboxLayer({
        id: id,
        type: GeoJsonLayer,
        data: data,
        // pickable: true,
        stroked: false,
        filled: true,
        extruded: true,
        // pointType: 'circle',
        lineWidthScale: 4,
        lineWidthMinPixels: 1,
        // getFillColor: [160, 160, 180, 200],
        getLineColor: d => calculateColor(d.properties.fftt, d.properties.weight),
        // getPointRadius: 100,
        getLineWidth: 1,
        getElevation: 30,
        // pointRadiusUnits: 'pixels',
    });
}

//------------------- Map data REST


function populateStreetsPerAreasStatic(zoom, areas, timestamp) {
    var decimateSkip = 18 - zoom;
    if (zoom < 15)
        decimateSkip = 5 - zoom;
    decimateSkip = 0
    console.log("Area number: " + areas.length + " Zoom level: " + zoom + " Decimate:" + decimateSkip + " Areas: " + areas);
    areas.forEach(area => {
        area = area.replace('-Northbound', '')
        console.log("area: " + area)
        var streetUrl = restServerLocation + promenadeBaseURI + 'streets?areaname=' + area + '&zoom=' + zoom + '&type=geojson&decimateSkip=' + decimateSkip + "&timestamp=" + timestamp;
        var layerId = 'myGeoJsonLayer_' + area
        var layer = createStreetLayer(layerId, streetUrl);
        myTrafficLayers.set(layerId, layer)
        map.addLayer(layer)
        map.moveLayer(layer.id, TRAFFIC_LAYER_PREDECESSOR)
    })
}

function drawStreetsStatic(timestamp) {
    var xhttp = new XMLHttpRequest(); // jshint ignore: line
    xhttp.contentType = "application/json";
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE) {
            if (xhttp.status === 200) {
                // console.log("response: " + this.responseText);
                var areas = JSON.parse(xhttp.responseText);
                var zoom = parseInt(map.getZoom())
                // var allStreets = null
                populateStreetsPerAreasStatic(zoom, areas, timestamp);
            } else {
                cleanMap(true, true);
                alert("Server cannot satisfy this request!");
            }
        }
    };
    var url = restServerLocation + promenadeBaseURI + 'areas?upperLeft=' + map.getBounds().getNorth() + ',' + map.getBounds().getWest() + '&lowerRight=' + map.getBounds().getSouth() + ',' + map.getBounds().getEast();
    xhttp.open('GET', url, true);
    xhttp.send();
}

function drawHeatmapStatic() {
    var topN = 156102;
    var dataURL = restServerLocation + promenadeBaseURI + 'intersections/topCritical?top=' + topN;
    var layer = new MapboxLayer({
        radiusPixel: 1000,
        type: HeatmapLayer,
        id: 'heatmap',
        data: dataURL,
        getPosition: d => [d.coordinate.longitude, d.coordinate.latitude, 0],
        getWeight: d => d.betweenness,
        aggregation: 'SUM',
    })
    map.addLayer(layer);
    map.moveLayer(layer.id, HEATMAP_LAYER_PREDECESSOR);
    myHeatmapLayers.set('heatmap', layer)
}

function populateHeatmapWithAreas(areas, normStrategy, endTimestamp) {
    console.log("in populate");
    // console.log(areas);
    // console.log(map)

    // console.log("AreaList: " + areaList);
    var xhttp = new XMLHttpRequest(); // jshint ignore: line
    xhttp.contentType = "application/json";
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE) {
            if (xhttp.status === 200) {
                var intersectionsList = JSON.parse(xhttp.responseText);
                intersectionsList.forEach(intersections => {
                    // console.log(intersections)
                    var areaname = intersections[0].areaName;
                    var heatmapLayer = new MapboxLayer({
                        radiusPixels: 20,
                        weightsTextureSize: 1024,
                        type: HeatmapLayer,
                        colorDomain: [0.05, 1],
                        id: 'heatmap_' + areaname,
                        data: intersections,
                        getPosition: d => [d.coordinate.longitude, d.coordinate.latitude, 0],
                        getWeight: d => d.betweenness,
                        aggregation: 'MEAN',
                    })
                    map.addLayer(heatmapLayer)
                    myHeatmapLayers.set('heatmap_' + areaname, heatmapLayer)
                    map.moveLayer(heatmapLayer.id, HEATMAP_LAYER_PREDECESSOR);

                });
            } else {
                cleanMap(true, true);
                alert("Server cannot satisfy this request!");
            }
        }
    };
    var url = restServerLocation + promenadeBaseURI + 'intersections/betweenness/?endTimestamp=' + endTimestamp + '&areas=' + areas + '&normStrategy=' + normStrategy;
    xhttp.open('GET', url, true);
    xhttp.send();


}

function drawHeatmapSnapshot(areaAggregationMode, time) {
    var xhttp = new XMLHttpRequest(); // jshint ignore: line
    xhttp.contentType = "application/json";
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE) {
            if (xhttp.status === 200) {
                var areas = JSON.parse(xhttp.responseText);
                console.log("areas: " + areas);
                populateHeatmapWithAreas(areas, areaAggregationMode, time)
            } else {
                cleanMap(true, true);
                alert("Server cannot satisfy this request!");
            }
        }
    };
    var url = restServerLocation + promenadeBaseURI + 'areas?upperLeft=' + map.getBounds().getNorth() + ',' + map.getBounds().getWest() + '&lowerRight=' + map.getBounds().getSouth() + ',' + map.getBounds().getEast() + '&mode=name';
    xhttp.open('GET', url, true);
    xhttp.send();
}


//------------------- Map data Websocket

const waitForOpenConnection = (socket) => {
    return new Promise((resolve, reject) => {
        const maxNumberOfAttempts = 10
        const intervalTime = 200 //ms

        let currentAttempt = 0
        const interval = setInterval(() => {
            if (currentAttempt > maxNumberOfAttempts - 1) {
                clearInterval(interval)
                reject(new Error('Maximum number of attempts exceeded'))
            } else if (socket.readyState === socket.OPEN) {
                clearInterval(interval)
                resolve()
            }
            currentAttempt++
        }, intervalTime)
    })
}

const sendMessage = async (socket, msg) => {
    if (socket.readyState !== socket.OPEN) {
        try {
            await waitForOpenConnection(socket)
            socket.send(msg)
            console.log("Message sent: " + msg)
        } catch (err) {
            console.error(err)
        }
    } else {
        socket.send(msg)
    }
}

function closeWebSocket() {
    if (socket) {
        socket.close();
        socket = null;
    }
}


function initializeWebSocket() {
    socket = new WebSocket(wsServerLocation + wsEndpoint);

    socket.addEventListener('open', function (event) {
        console.log("Connection opened!: " + event);
    });

    socket.addEventListener('close', function (event) {
        console.log("Connection closed!: " + event);
    });

    socket.addEventListener('message', function (event) {
        var areaname = event.data.match("(areaName=)([0-9a-zA-Z-_]+)")[2]
        var linkid = parseInt(event.data.match("(linkid=)([0-9-]+)")[2])
        var tt = parseFloat(event.data.match("(avgTravelTime=)([0-9\.]+)")[2])

        var layerId = 'areaLayer_' + areaname
        var layer = myTrafficLayers.get(layerId)
        var featureCollection = layer.props.data.features
        var flag = false
        for (let feature of featureCollection) {
            if (feature.properties.id === linkid) {
                console.log("areaname: " + areaname + ", linkid: " + linkid + ", weight: " + feature.properties.weight + ", tt: " + tt + ", fftt: " + feature.properties.fftt);
                feature.properties.weight = tt;
                flag = true;
                break;
            }
        }
        if (flag) {
            map.removeLayer(layer.id)
            var layer2 = createStreetLayer(layerId, {features: featureCollection, type: "FeatureCollection"})
            // var layer2 = new MapboxLayer({
            //     id: 'areaLayer_' + areaname,
            //     type: GeoJsonLayer,
            //     data: {features: featureCollection, type: "FeatureCollection"},
            //     pickable: true,
            //     stroked: false,
            //     filled: true,
            //     extruded: true,
            //     pointType: 'circle',
            //     lineWidthScale: 5,
            //     lineWidthMinPixels: 1,
            //     getFillColor: [160, 160, 180, 200],
            //     getLineColor: d => calculateColor(d.properties.fftt, d.properties.weight),
            //     getPointRadius: 100,
            //     getLineWidth: 1,
            //     getElevation: 30,
            //     pointRadiusUnits: 'pixels',
            // });
            myTrafficLayers.set(layerId, layer2)
            map.addLayer(layer2)
            map.moveLayer(layer2.id, TRAFFIC_LAYER_PREDECESSOR)
        }
    });
    socket.addEventListener('error', function (event) {
        console.log("Errore della websocket: ", event);
        closeWebSocket()
    });
}

function populateAreasDynamic(zoom, areas) {
    var decimateSkip = 4;
    areas.forEach(area => {
        // var url = restServerLocation + baseURIRestRoadNetwork + 'streets?areaname=' + area + '&zoom=' + zoom + '&type=geojson&decimateSkip=' + decimateSkip + '&upperLeft=' + map.getBounds().getNorth() + ',' + map.getBounds().getWest() + '&lowerRight=' + map.getBounds().getSouth() + ',' + map.getBounds().getEast();
        var url = restServerLocation + promenadeBaseURI + 'streets?areaname=' + area + '&zoom=' + zoom + '&type=geojson&decimateSkip=' + decimateSkip;
        var xhttp = new XMLHttpRequest();
        xhttp.contentType = "application/json";
        xhttp.onreadystatechange = function () {
            if (xhttp.readyState === XMLHttpRequest.DONE) {
                if (xhttp.status === 200) {
                    var featureCollection = JSON.parse(xhttp.responseText);
                    if (featureCollection.features) {
                        var layerId = 'areaLayer_' + area;
                        var layer = createStreetLayer(layerId, featureCollection)
                        console.log(layer)
                        myTrafficLayers.set(layerId, layer)
                        map.addLayer(layer)
                        map.moveLayer(layer.id, TRAFFIC_LAYER_PREDECESSOR)
                    }
                } else {
                    cleanMap(true, true);
                    alert("Server cannot satisfy this request!");
                }
            }
        };
        xhttp.open('GET', url, false);
        xhttp.send();
    })
}


//------------------- Button Listeners

staticTrafficButton.addEventListener('click', ev => {
        console.log("staticTrafficButton clicked")
        closeWebSocket()
        if (overlapCheckbox.checked) {
            cleanMap(false, true);
        } else {
            cleanMap(true, true);
        }
        // TODO
        var date = datePicker.value
        var time = timePicker.value
        var d = new Date(date + " " + time); // Your timezone!
        var endTimestamp = d.getTime() / 1000.0;
        // console.log(endTimestamp)
        drawStreetsStatic(endTimestamp);
    }
)
/*
heatMapStaticButton.addEventListener('click', ev => {
        console.log("staticTrafficButton clicked")
        closeWebSocket()
        cleanMap()
        drawHeatmapStatic();
    }
)
 */
/*heatMapDynamicButton.addEventListener('click', ev => {
        console.log("dinamicTrafficButton clicked")
        closeWebSocket()
        cleanMap()
        drawHeatmapSnapshot();
    }
)*/

heatMapSnapshotButton.addEventListener('click', ev => {
        console.log("heatMapSnapshotButton clicked")
        closeWebSocket()
        if (overlapCheckbox.checked) {
            cleanMap(true, false)
        } else {
            cleanMap(true, true)
        }
        var date = datePicker.value
        var time = timePicker.value

        var d = new Date(date + " " + time); // Your timezone!
        var endTimestamp = d.getTime() / 1000.0;
        var normType = null;
        radioNorm.forEach(radio => {
            if (radio.type === "radio") {
                if (radio.checked) {
                    normType = radio.value
                }
            }
        });
        console.log("End Time: " + endTimestamp + ", Normalization Strategy: " + normType);
        drawHeatmapSnapshot(normType, endTimestamp);

    }
)

startTrafficButton.addEventListener('click', ev => {
    closeWebSocket()
    cleanMap(true, true)
    // trafficLegendDiv.removeAttribute("hidden");
    initializeWebSocket()

    var areas;
    var xhttp = new XMLHttpRequest(); // jshint ignore: line
    xhttp.contentType = "application/json";
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === XMLHttpRequest.DONE) {
            if (xhttp.status === 200) {
                // console.log("response: " + this.responseText);
                areas = JSON.parse(xhttp.responseText);
                var zoom = parseInt(map.getZoom())
                populateAreasDynamic(zoom, areas);
            } else {
                cleanMap(true, true);
                alert("Server cannot satisfy this request!");
            }
        }
    };
    xhttp.open('GET', restServerLocation + promenadeBaseURI + 'areas?upperLeft=' + map.getBounds().getNorth() + ',' + map.getBounds().getWest() + '&lowerRight=' + map.getBounds().getSouth() + ',' + map.getBounds().getEast(), false);
    xhttp.send();
    var subscriptions = []
    areas.forEach(a => {
        // sendMessage(socket, a)
        subscriptions.push(a)
    })
    var msg = subscriptions.join(",")
    console.log("Subscribing to: " + msg)
    sendMessage(socket, msg)

})

cleanMapButton.addEventListener('click', ev => {
    closeWebSocket();
    cleanMap(true, true);
});

stopTrafficButton.addEventListener('click', ev => {
    closeWebSocket()
});


toggleAreas.addEventListener('change', ev => {
    if (toggleAreas.checked) {
        console.log('Areas on!')
        var xhttp = new XMLHttpRequest(); // jshint ignore: line
        xhttp.contentType = "application/json";
        xhttp.onreadystatechange = function () {
            if (xhttp.readyState === XMLHttpRequest.DONE) {
                if (xhttp.status === 200) {
                    // console.log("response: " + this.responseText);
                    var areas = JSON.parse(xhttp.responseText);
                    var areaList = '';
                    areas.features.forEach(area => {
                        var areaname = area.properties.name;
                        areaList = areaList + areaname + ',';
                        var c1 = Math.floor(Math.random() * 255);
                        var c2 = Math.floor(Math.random() * 255);
                        var c3 = Math.floor(Math.random() * 255);

                        var polygonLayer = new MapboxLayer({
                            type: GeoJsonLayer,
                            getFillColor: [c1, c2, c3, 40],
                            getLineColor: [c1, c2, c3, 100],
                            // getFillColor: [0, 0, 0, 0],
                            id: 'poligon_' + areaname,
                            lineWidthScale: 5,
                            lineWidthMinPixels: 1,
                            data: area,
                        })
                        map.addLayer(polygonLayer);
                        map.moveLayer(polygonLayer.id, POLYGON_LAYER_PREDECESSOR);
                        myPolygonLayers.set('poligon_' + areaname, polygonLayer)
                    });
                    console.log(map)
                } else {
                    cleanMap(true, true);
                    alert("Server cannot satisfy this request!");
                }
            }
        };
        var url = restServerLocation + promenadeBaseURI + 'areas?upperLeft=' + map.getBounds().getNorth() + ',' + map.getBounds().getWest() + '&lowerRight=' + map.getBounds().getSouth() + ',' + map.getBounds().getEast() + '&mode=geojson';
        xhttp.open('GET', url, true);
        xhttp.send();
    } else {
        myPolygonLayers.forEach(layer => {
            map.removeLayer(layer.id)
        })
        myPolygonLayers = new Map()
        console.log("Areas off!");
    }
})
