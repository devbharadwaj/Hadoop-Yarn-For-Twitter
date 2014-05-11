
twittergo = function(){
	console.log('Inside Twitter go');
	var key = '';
	var percent = 0;
	var done = false;
	function process_start() {
		$.getJSON('http://localhost:5000/start',
            	function(data) {
			console.log('start');
			key = data.key;
			percent = data.percent;
			done = data.done;
			$("#progressbar").progressbar({value:percent});
                        process_progress(key);
		});//JSON data.key
	}//process-start function

	function process_progress(key) {
		$.getJSON('http://localhost:5000/progress',
		{
		'key': key
		} ,
		function(data) {
			console.log('progress');
			key = data.key;
			percent = data.percent;
			done = data.done;
			$("#progressbar").progressbar({value:percent});
			if (!data.done) {
				setTimeout(function() {
	                        	process_progress(key);
	                	}, 100);
			}// If not done, recursive call
		});//JSON percentage	
	}//process-progress function

	process_start();
}//twittergo


$(function() {
	$("#progressbar").progressbar({value: 0});
	$("#getTweets").click(twittergo);	
});


