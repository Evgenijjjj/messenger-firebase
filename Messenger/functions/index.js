'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Notifications/{receiver_user_id}/{notification_id}')
.onWrite((data, context) =>
         {
         const receiver_user_id = context.params.receiver_user_id;
         const notification_id = context.params.notification_id;
         
         console.log('We have Notification to send to:', receiver_user_id);
         
         if (!data.after.val()) {
         console.log('notification has been deleted:', notification_id);
         return null;
         }
         
         const MessageText = admin.database().ref(`/Notifications/${receiver_user_id}/${notification_id}`).once(`value`);
         var msg_text = "new Message !";
         var receiver_username = "Your friend";
         MessageText.then(res =>
                          {
                          msg_text = res.child(`text`).val();
                          });
         
         const DeviceToken = admin.database().ref(`/users/${receiver_user_id}`).once(`value`);
         
         return DeviceToken.then(result =>
                                 {
                                 const token_id = result.child(`messagingToken`).val();
                                 receiver_username = result.child(`username`).val();
                                 
                                 const payload = {
                                 notification: {
                                 title: receiver_username,
                                 body: msg_text,
                                 icon: "default"
                                 }
                                 };
                                 
                                 return admin.messaging().sendToDevice(token_id, payload)
                                 .then(response => {
                                       console.log('This was a notificaton future.');
                                       });
                                 });
         });

