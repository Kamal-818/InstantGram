$(function() {
	$("#nav").load("/nav");
});

function GetSortOrder(prop) {
	return function(a, b) {
		if (a[prop] > b[prop]) {
			return -1;
		} else if (a[prop] < b[prop]) {
			return 1;
		}
		return 0;
	}
}

$(document).ready(function() {
	
    putLoader('loader_div');

	loadPosts();

	$("form#upload").submit(function(e) {
		e.preventDefault();
		var formData = new FormData(this);

		if (confirm("Are you sure you want to upload this picture?")) {
			
			showLoader();
			
			$.ajax({
				url : '/upload',
				type : 'post',
				data : formData,
				async: false,
				success : function(data) {
					console.log(data);
					loadPosts();
					hideLoader();
				},
				cache : false,
				contentType : false,
				processData : false
			});
		}
	});

});

function loadPosts() {
	$("#posts").html("");
	$
			.ajax({
				url : '/posts/' + username,
				type : 'get',
				success : function(data) {

					data.sort(GetSortOrder("timestamp"));
					console.log(data);

					$("#postCount").html(data.length);
					for (var i = 0; i < data.length; i++) {
						$("#posts")
								.append(
										' <div class="col-4 post"><img id="'
												+ data[i].id
												+ '" src="'
												+ data[i].picPath
												+ '"  onclick="changeModal(this);" data-toggle="modal" data-target="#postModal"></div>');
					}
				},
				fail : function(data) {
					console.log("fetch failed" + data);
				}
			});

}

function changeModal(post) {
	$("#modal_image").attr("src", post.src);
	window.mpostid = post.id;
	console.log(post.id);

}

function deletePost() {

	if (confirm("Are you sure you want to delete this picture?")) {
		$.ajax({
			url : "/posts/" + window.mpostid,
			type : 'delete',
			async : false,
			success : function(data) {
				console.log(data);
			}
		});

		location.reload();
	}
}
