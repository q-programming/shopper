var default_username = 'jan@nowak.pl';
var default_password = 'FbtWK8BT`G5%G+jD2y9p@1!$z9C@YbVlX{HWt&}t*{$use)HmjC)[*Hj`ZD(ALoM';
var listID = 1;
var debug = false;
//var baseURL = 'http://192.168.1.3:8080/shopper';
var baseURL = 'https://q-programming.pl/shopper';
var baseAPI = baseURL + '/api';

var router;

/**
 * Get paramater from url
 */
function getUrlParameter(sParam) {
	var sPageURL = window.location.search.substring(1), sURLVariables = sPageURL
			.split('&'), sParameterName, i;

	for (i = 0; i < sURLVariables.length; i++) {
		sParameterName = sURLVariables[i].split('=');

		if (sParameterName[0] === sParam) {
			return sParameterName[1] === undefined ? true
					: decodeURIComponent(sParameterName[1]);
		}
	}
};

function logger(msg, obj) {
	if (debug) {
		console.log('[DEBUG]' + msg, obj ? obj : '')
	}
}
