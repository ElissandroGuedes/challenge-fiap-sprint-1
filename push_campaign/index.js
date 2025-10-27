const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendCampaignPush = functions.firestore
  .document("campaigns/{campaignId}")
  .onCreate(async (snap, context) => {
    const campaign = snap.data();

    try {
      const usersSnapshot = await admin.firestore().collection("users")
        .where("tipo", "==", "cliente")
        .where("fcmToken", "!=", null)
        .get();

      let successCount = 0;

      for (const doc of usersSnapshot.docs) {
        const token = doc.data().fcmToken;

        const message = {
          token: token,
          notification: {
            title: campaign.title || "Nova campanha",
            body: campaign.body || "Confira os detalhes",
            

          },
          data: {
                title: campaign.title,
                body: campaign.body,
                url: campaign.url,
                buttonLabel: campaign.buttonLabel || "Saiba mais",
                actions: JSON.stringify(campaign.actions),
                actionUrls: JSON.stringify(campaign.actionUrls),
                payload: JSON.stringify(campaign)
              }
        };

        try {
          await admin.messaging().send(message);
          successCount++;
        } catch (error) {
          console.error(`Erro ao enviar para ${token}:`, error.message);
        }
      }

      console.log("Mensagens enviadas com sucesso:", successCount);
    } catch (error) {
      console.error("Erro ao processar campanha:", error.message);
    }
  });