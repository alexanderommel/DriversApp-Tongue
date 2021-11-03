# Android-Drivers

Android App thats integrated with Tongue Shipping Service used to provide an interface for the customers of any client app such as Tongue Customers App, and the drivers (As Uber does with the communication between Uber Eats App and Uber Drivers App). In this scenario, the communication is done throught Tongue Shipping Service and Tongue Shopping Service. 

## Features

- Developed on Android

- Single Activity Architecture

- Geolocation

- HTTP Communication

- Google OAuth 2.0

- Real time communication with Shipping Service using Websockets (STOMP)

- Google Maps

- Authentication with Shipping API (Backend) using JWT

- Delivery App for drivers

## How it works

1. Start the app, login in with an authorized Google Account in your Google OAuth Client.

2. Press the button 'Connect' and the stomp client will start the communication with the backend.

3. Once a customer called the endpoint GET: /shipping/request_driver on the server, if your current location is inside the area of the origin (location of the person who requested a shipping service), your app will be notified to accept the request.

4. If you accept the request, you will be notified with all the details of the request.

5. To continue the shipping and get the customer location, your current location must be near the location obtained. You can set your location to the sender location touching a button on the navigation panel.

6. A 'Finish' button will be created to let you finish the delivery.

## Installation

1. Create a new project in Google Cloud Platform

2. Create an OAuth client using Google Cloud Platform and copy the ID in the tag 'server_client_id' inside the file strings.xml.

3. Create a Google Maps Client with Google Cloud Platform and copy the API KEY in the file google_maps_api.xml.

4. Run the project 'Tongue Shipping Service' (A redis server must be running in paralell)

5. Configure the class TongueNetworkSettings with the IP and Port of the running server.

6. Install the App


## Contributing
Personal project,but open to any help 💤

## License
[Do What The F*ck You Want To Public License](http://www.wtfpl.net/)
