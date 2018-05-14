## AlamoHunt

This app searches for places and recommendations using Foursquare's places API for Austin venues of 
interest, such as Tacos. Displays ratings for each venue, distance from the center of Austin and
ability to favorite any venue, which persists between use of the app.

- Uses live autocomplete search in main screen dropdown menu
- Main screen list shows venue details such as Distance from Austin, Rating, If Favorited by user
- Shows map of all venues via a floating action button.
- Map allows selection of individual venue & Center of Austin
- Each individual venue activity shows details of venue and allow to view on Foursquare site
  or the website for the venue, or the menu if available
- Uses collaping menu to show individual venue details
- Properly adheres to android UI standards

# APIs & Libraries
- Picasso for async image loading
- Lottie for animation
- Retrofit for async endpoint loading and forming queries
  - Good example of chaining requests asynchronously
- Handles back button and search button for virtual keyboard
- Snaps the Place picker list back to proper position after navigating screens


# Refactors for version 2
- possibly use only one kind of venue object for the Foursquare data and the internal data
- Make UI prettier
- Add more details to individual venue details activity
- Using retrofit more efficiently
  - Currently it loads a venue then must go get the details about each venue from a different endpoint
  - There may be a better way to do this
  
