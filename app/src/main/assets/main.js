var map = null;
var markerIcon = null;
var marker = null;

$(document).ready(function(){
    // init marker icon
    markerIcon = L.icon({
        iconUrl: 'img/location_indicator.png',
        // no need shadow
        //shadowUrl: 'leaf-shadow.png',

        iconSize:     [48, 48], // size of the icon
        //shadowSize:   [50, 64], // size of the shadow
        iconAnchor:   [24, 24] // point of the icon which will correspond to marker's location
    });

	// init the map
	map = L.map('mapid', {
		crs: L.CRS.Simple
	});

	var bounds = [[0,0], [401,312]]; // [height, width]
	// add image to map
	var image = L.imageOverlay('img/floor_plan.png', bounds).addTo(map);
	map.fitBounds(bounds);

	// set marker
	//marker = L.marker([371, 178], {icon: markerIcon});
  //marker.addTo(map).bindPopup('You are around here!'); // y, x in pixel

	/*var driver = L.latLng([ 378, 178 ]); // y, x
	marker = L.marker(driver);
	marker.addTo(map).bindPopup('driver');*/

	// center the view according to the marker
	// map.setView( [210, 110], 1);
});

/*
Current location point update
input object array = [
{bid: 1, rssi: -99},
{bid: 2, rssi: -99},
{bid: 3, rssi: 0},
{bid: 4, rssi: 0},
{bid: 5, rssi: -99}
];
*/
function updateMarker(arr){ // arr must be json array string

  var result = findClosestBeaconAsPoint(JSON.parse(arr));

	// remove previous marker
  console.log("remove previous marker");
  if(marker != null){
    map.removeLayer(marker);
  }

	// re-print the marker
	marker = L.marker([result.y, result.x], {icon: markerIcon});
  marker.addTo(map).bindPopup('You are around here!'); // y, x in pixel

	// center the view according to the marker
	// map.setView( [y, x], 1);
}

/*
received_beacons = [
{bid: 1, rssi: -99},
{bid: 2, rssi: -99},
{bid: 3, rssi: 0},
{bid: 4, rssi: 0},
{bid: 5, rssi: -99}
];
*/
function findClosestBeaconAsPoint(received_beacons){
  var min_rssi = Number.MAX_SAFE_INTEGER;
  var closestBeacon;
  for(var i = 0; i < received_beacons.length; i++){
    var beacon = received_beacons[i];
    if(beacon.rssi < min_rssi){
      min_rssi = beacon.rssi;
      closestBeacon = beacon;
    }
  }
  var result = {
    "bid": -1,
    "rssi": 0,
    "x": 0,
    "y": 0,
    "matched": false
  };
  for(var i = 0; i < deployed_beacons.length; i++){
    if(closestBeacon.bid == deployed_beacons[i].bid){
      result.bid = deployed_beacons[i].bid;
      result.rssi = closestBeacon.rssi;
      result.x = deployed_beacons[i].x;
      result.y = deployed_beacons[i].y;

      result.matched = true;
      break;
    }
  }

  return result;
}
