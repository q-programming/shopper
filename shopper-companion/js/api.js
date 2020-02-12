function get(url, successCallback, errorMsg) {
	$('#loader').show();
	$.ajax({
		url : url,
		type : "GET",
		dataType : 'json',
		crossDomain : true,
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", "Basic "
					+ btoa(user + ":" + deviceID));
		},
		success : function(response) {
			$('#loader').hide();
			successCallback(response)
		},
		error : function(error) {
			console.log(error);
			$('#loader').hide();
			alert(errorMsg);
		}
	});
}

function post(url, body, successCallback, errorMsg) {
	$('#loader').show();
	$.ajax({
		url : url,
		type : "POST",
		contentType : "application/json",
		data : JSON.stringify(body),
		crossDomain : true,
		success : function(response, status, request) {
			$('#loader').hide();
			successCallback(response);
		},
		error : function(error) {
			console.log(error);
			$('#loader').hide();
			alert(errorMsg);
		}
	});
}
