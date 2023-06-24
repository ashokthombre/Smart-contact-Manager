console.log("User login")

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	}
	else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");

	}

};

const search = () => {
	//console.log("searching")

	let query = $("#search-input").val();


	if (query == "") {
		$(".search-result").hide();

	}
	else {
		//console.log(query);
		//sending request to server

		let url = `http://localhost:8080/search/${query}`
		fetch(url).then(response => {
			return response.json();

		}).then(data => {



			//  console.log(data);

			let text = `<div class='list-group'>`

			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name}</a>`

			});


			text += `</div>`
			$(".search-result").html(text);
			$(".search-result").show();

		});

		$(".search-result").show();

	}


};


//first request to server to create order

const paymentStart = () => {
	console.log("payment started");

	let amount = $("#payment_field").val();

	console.log(amount);

	if (amount == '' || amount == null) {
		swal("Failed!", "Amount is required.. !", "error");
		return;

	}
	//code

	//we will ajax to send request to server to create order

	$.ajax(

		{

			url: '/user/create_order',
			data: JSON.stringify({ amount: amount, info: 'order_request' }),
			contentType: 'application/json',
			type: 'POST',
			dataType: 'json',
			success: function(response) {
				//invoked when success
				console.log(response);

				if (response.status == "created") {

					//open payment form
					var options = {
						"key": "rzp_test_9KZkqRrN8EycDe",
						"amount": response.amount,

						"currency": "INR",
						"name": "Ashok Thombre",
						"description": "Donation",

						"order_id": response.id,

						"handler": function(response) {
							console.log(response.razorpay_payment_id);
							console.log(response.razorpay_order_id);
							console.log(response.razorpay_signature)


                        updatePaymentOnServer(
						response.razorpay_payment_id,
						response.razorpay_order_id,
						"paid"
						);


							
						},
						"prefill": {
							"name": "",
							"email": "",
							"contact": ""
						},
						"notes": {
							"address": "Razorpay Corporate Office"

						},
						"theme": {
							"color": "#3399cc"
						}
					};
					var rzp1 = new Razorpay(options);
					rzp1.on('payment.failed', function(response) {
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						swal("Failed!", "Oops Payment Failed!", "error");
					});
					rzp1.open();


				}


			},
			error: function(error) {
				//invoked when error
				console.log(error)
				alert("Something went wrong");

			}


		}

	)





};

function updatePaymentOnServer(payment_id,order_id,status)
{

	$.ajax({

       
			url: '/user/update_order',
			data: JSON.stringify({ payment_id: payment_id,order_id: order_id,status:status }),
			contentType: 'application/json',
			type: 'POST',
			dataType: 'json',

			success:function(response){
				swal("Payment Successful.!", "Your Payment Successfully Completed..!", "success");

			},
			error:function(error){

				swal("Failed!", "Payment Successful, but we did not get on server", "error");

			},


	})

}














