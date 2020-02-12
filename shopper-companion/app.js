page = document.getElementById('main');
var snapListviewWidget;
var progressBarWidget;
var rotaryDetentHandler;
var name_buffer;
var items;
var refreshing = false;
var deviceID;
var user;

(function() {
	/**
	 * Back key event handler
	 */
	window
			.addEventListener(
					'tizenhwkey',
					function(ev) {
						if (ev.keyName === "back") {
							var page = document
									.getElementsByClassName('ui-page-active')[0], pageid = page ? page.id
									: "";
							if (pageid === "main") {
								try {
									tizen.application.getCurrentApplication()
											.exit();
								} catch (ignore) {
								}
							} else {
								logger('Remove rotary detent')
								document.removeEventListener("rotarydetent",
										rotaryDetentHandler);
								router = '';
								window.history.back();
								loadData();
							}
						}
					});
	//	Prevent application going to sleep to fast
	try {
		tizen.power
				.setScreenStateChangeListener(function(prevState, currState) {
					if (currState === 'SCREEN_NORMAL'
							&& prevState === 'SCREEN_OFF') {
						// when screen woke up
						var app = tizen.application.getCurrentApplication();
						tizen.application.launch(app.appInfo.id, function() {
							refreshing = true;
							loadData();
						})
					}
				});
	} catch (e) {
	}

	page.addEventListener("pagebeforeshow", function() {

	});

	page.addEventListener("scroll", function() {
		// console.log(snapListviewWidget);
	});

	page.addEventListener("pagehide", function() {
		// logger('Remove rotary detent')
		// document.removeEventListener("rotarydetent", rotaryDetentHandler);
	});

	/**
	 * End init listeners
	 */

	$("#splash").hide();
	/* get device and email */
	//TODO remove this
//	localStorage.deviceId = default_password 
//	localStorage.username = default_username
	//TODO remove this

	
	deviceID = localStorage.deviceId
	user = localStorage.username

	logger('Device ID', deviceID)
	logger('User ID', user)
	if (!deviceID || !user) {
		tizen.systeminfo.getPropertyValue("BUILD", onSuccessCallback, onErrorCallback);
		alert('This device has not yet been registered. Please input your email address and follow sent instructions ')
		$("#register").show();
		enableRegister();
	} else {
		loadData();
	}
	
    function onSuccessCallback(device) {
    	localStorage.deviceName = device.manufacturer + ' ' + device.model 
    }
    
     function onErrorCallback(error) {
        alert("Not supported: " + error.message);
    }
    

	/**
	 * Handle loose of visibility change
	 */
	// document.addEventListener("visibilitychange", function() {
	// console.log("visibilitychange");
	// if (document.hidden) {
	// logger('document hidden');
	// } else {
	// logger('document visible');
	// refreshing = true;
	// loadData();
	// }
	// }, false);
}());

function enableRegister(){
	$("#register").submit(function(e){
        e.preventDefault();
        $("#loader").show();
        const email = $("#email").val();
        post(baseURL + "/auth/new-device",{email:email,name: localStorage.deviceName},registerSuccessfull,"There were errors while trying to register new device!");
    });
}

function registerSuccessfull(response){
	logger(response.plainKey);
	localStorage.deviceId = response.plainKey;
	localStorage.username = response.email;
	$("#register").hide();
	$("#pending").show();
	alert('You should recieve confirmation email to confirm new device. Restart application after confirmation is done')
}


function loadData() {
	deviceID = localStorage.deviceId
	user = localStorage.username
	if (router) {
		navigate();
	} else {
		getLists();
	}
}

function navigate() {
	$('#lists').empty();
	logger('List was clicked , navigating on: ' + router);
	getListData(router);
}

/**
 * Fetch lists array data from API
 */
function getLists() {
	get(baseAPI + "/list/mine",renderLists,"There were errors while trying to fetch lists, or device has not yet been confirmed ")
}

/**
 * Renders all lists in form of li
 * 
 * @param lists
 *            array of lists
 */
function renderLists(lists) {
	Cookies.set('TESTING', lists[0].ownerId);
	$('#lists').empty();
	list = document.querySelector(".ui-listview");
	if (list) {
		snapListviewWidget = tau.widget.SnapListview(list);
	}
	lists
			.forEach(function(listData) {
				var marquee = '';
				if (listData.name.length > 15) {
					marquee = ' marquee ui-marquee ui-marquee-gradient';
				}
				var item = '<a href="items/index.html" data-list="'
						+ listData.id
						+ '" class="ui-li-cell content list-nav'
						+ marquee
						+ '">'
						+ '<img src="css/img/list.jpg" class="list-icon"/><span class="list-name">'
						+ listData.name + '</span>' + '</a>';
				$('#lists').append(
						'<li class="list-navigate ui-snap-listview-item">'
								+ item + '</li>');
			});
	enableListsClicks();
	snapListviewWidget.refresh();
	snapListviewWidget.scrollToPosition(0);
}

/**
 * Load data for list
 * 
 * @param listID
 */
function getListData(listID) {
	$('#loader').show();
	get(baseAPI + "/list/" + listID,renderListItems,"There were errors while trying to fetch list items ");
}

/**
 * Render all lists items data While rendering , also set correctly done items
 * 
 * @param listData
 *            array of list items
 */
function renderListItems(listData) {
	items = listData.items
	list = document.querySelector(".items.ui-listview");
	if (list) {
		snapListviewWidget = tau.widget.SnapListview(list);
	}
	$('#list_name').text(listData.name);
	$('#items').empty();
	var done = 0;
	items.forEach(function(item) {
		var desc = '';
		var check = '';
		var qu = quantityAndUnit(item);
		var marquee = (item.name.length + qu.length > 14)?' marquee ui-marquee ui-marquee-gradient':'';
		if (item.description) {
			var marqueedesc = (item.description.length > 20)?'marquee-desc':'';
			desc = '<div class="ui-li-sub-text li-text-sub">'
					+ '<span class="'+ marqueedesc + '">'
					+ item.description 
					+ '</span></div>'
		}
		if (item.done) {
			check = ' checked'
			done++;
		}
		var item = '<div class="ui-li-table">'
				+ '<div class="ui-li-row shopping-item" data-id="' + item.id
				+ '">' + '<div class="ui-li-cell toggle">'
				+ '<input class="item-toggle" type="checkbox" ' + check + '/>'
				+ '</div>' + '<div class="ui-li-cell content ' + marquee
				+ '"><span class="name-content">' 
				+ qu
				+ item.name + '</span>'
				+ desc + '</div>' + '</div>' + '</div>';

		$('#items').append(
				'<li class="ui-snap-listview-item">' + item + '</li>');
	});
	snapListviewWidget.refresh();
	if (!refreshing) {
		snapListviewWidget.scrollToPosition(0);
	} else {
		refreshing = false
	}
	enableItemsClicks();
	enableRotaryEvent();
	$('#items').data('done', done)
	// progressBar = document.getElementById("circleprogress");
	// progressBarWidget = new tau.widget.CircleProgressBar(progressBar, {
	// size : "full"
	// });
	// progressBarWidget.value((done / items.length) * 100);
}
function quantityAndUnit(item){
	var qu = ''
	if(item.quantity && item.quantity >0){
		qu = '' + item.quantity
	}
	if(item.unit){
		qu = qu + item.unit
	}
	return qu!=''?qu + ' ':''
}

/**
 * Add extra events on clicking on navlink
 */
function enableListsClicks() {
	$('.list-nav').on('click', function() {
		router = $(this).data('list')
		navigate();
	});
}

/**
 * Enable clicks on all items to toggle done/not done
 */
function enableItemsClicks() {
	$('.shopping-item').on(
			'click',
			function() {
				var $tc = $(this).find('input:checkbox:first'), tv = $tc
						.attr('checked');
				const url = baseAPI + "/item/" + router + "/toggle/" + $(this).data("id");
				$.ajax({
					url : url,
					type : "GET",
					beforeSend : function(xhr) {
						xhr.setRequestHeader("Authorization", "Basic "
								+ btoa(user + ":" + deviceID));
					},
					xhrFields : {
						withCredentials : true
					},
					crossDomain : true,
					success : function(item) {
						logger('Toggle item : ', item);
						$tc.attr('checked', item.done);
						var done = $('#items').data('done');
						if (item.done) {
							done++;
						} else {
							done--;
						}
						$('#items').data('done', done);
						// updateProgress();
					},
					error : function(response) {
						console.log(response);
						$('#loader').hide();
						alert("There were errors while trying to toggle item");
					}
				});
				$tc.attr('checked', !tv);
			});
}


function enableRotaryEvent() {
	// var firstEl = snapListviewWidget._listItems[0].element
	// $(firstEl).toggleClass('ui-snap-listview-selected');

	rotaryDetentHandler = function(e) {
		// Get rotary direction
		direction = e.detail.direction;
		var itemIndex = snapListviewWidget.getSelectedIndex();
		logger('Selected item:', itemIndex);
		var length = snapListviewWidget._listItems.length
		var el;
		if (direction === "CW") {
			// Right direction snapListviewWidget
			if (itemIndex < length - 1) {
				el = snapListviewWidget._listItems[itemIndex + 1].element
			} else {
				el = snapListviewWidget._listItems[length - 1].element
			}
		} else if (direction === "CCW") {
			// Left direction
			if (itemIndex > 0) {
				el = snapListviewWidget._listItems[itemIndex - 1].element
			} else {
				el = snapListviewWidget._listItems[0].element
			}
		}
		$(el).toggleClass('ui-snap-listview-selected');
	};
	document.addEventListener("rotarydetent", rotaryDetentHandler);
}

function updateProgress() {
	var done = $('#items').data('done');
	progressBarWidget.value((done / items.length) * 100);
	if (done == items.length) {
		var pop = document.getElementById("graphicPopupToast");
		tau.openPopup(pop);
		setTimeout(function() {
			tau.closePopup(pop);
		}, 3000);
	}
}
