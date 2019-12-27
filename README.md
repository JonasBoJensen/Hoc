# Hoc

Splash activity / activity_splash
Splash activity is the main activity, it layout is the activity_splash xml, the activity_splash xml holds the imageview with the image
for the splash screen. then the activity controls the fadein and fadeout Transition using the fadein.xml and fadeout.xml 
located in res/anim folder, then moves the user to the MainActivity

MainActivity / activity_main
this activitys function is to sign in users to access the firebase database, using google and facebook sign in 
activity holds the two buttons used to sign in with google or facebook. if successful the user is directed to the loggedActivity
Inside this activity there is more indept descriptions for the methods and class

LoggedActivity / Activity_logged
In the top of this activity is a text with with the text hey (username) and their photo, this is based on the users profile picture and name
below that, the user is able to add new rooms with the desired room name, it is then added to the firebase database, and a click event is added to every 
room that is showen for the user, this makes the user able to click on a room and is then moved to the database child with that rooms name.
at the bottom there is a log out buttom which logs out the user and moves them back to maianctivity.
Inside this activity there is more indept descriptions for the methods and class

ChatRoomActivity / activity_chat_room
This is the chat room where users as able to send messages and information to the Database and to see the messages in that chat room.
in the top is shows the room name so the user is able to see which he or she currently is in.
in the middle is all the chat messages, they are build by reading the information from the database, then use a FirebaseRecyclerAdapter
to build up the messages using the model ChatMessage for getting and setting the disired information, and then use the item_message.xml 
as a layout for how to arrange the information. then the populates the recicleview with the messages
at the bottom the user is able to write the messages
more information on methods and class in code


