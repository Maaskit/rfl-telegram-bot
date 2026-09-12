const tg = window.Telegram.WebApp;

tg.ready();
tg.expand();


const user = tg.initDataUnsafe?.user;


if (user) {

    const username =
        user.first_name
            ? `, ${user.first_name}`
            : "";

    document.getElementById("username").textContent = username;
}


/* -----------------------------
   DEMO DATA
----------------------------- */

let appState = {

    streak: 7,

    notificationTime: "12:00",

    notificationsEnabled: true,

    rewardClaimed: false

};


/* -----------------------------
   CLAIM
----------------------------- */

function claimReward() {

    if (appState.rewardClaimed) {

        tg.showAlert(
            "Ты уже забрал сегодняшнюю награду ✅"
        );

        return;
    }

    appState.rewardClaimed = true;

    document.getElementById(
        "rewardStatus"
    ).textContent =
        "Сегодняшняя награда уже получена ✅";

    const button =
        document.getElementById("claimButton");

    button.textContent = "ПОЛУЧЕНО";

    button.disabled = true;

    tg.showPopup({

        title: "Награда получена 🎉",

        message:
            "Отлично! Не забудь вернуться завтра.",

        buttons: [
            {
                id: "ok",
                type: "ok"
            }
        ]

    });
}


/* -----------------------------
   STREAK
----------------------------- */

function showStreak() {

    tg.showAlert(
        `🔥 Твоя серия: ${appState.streak} дней`
    );
}


/* -----------------------------
   TIME
----------------------------- */

function showTimeSettings() {

    tg.showPopup({

        title: "Время оповещения",

        message:
            `Сейчас: ${appState.notificationTime}\n\nНастройку сделаем следующим этапом.`,

        buttons: [
            {
                id: "ok",
                type: "ok"
            }
        ]

    });
}


/* -----------------------------
   NOTIFICATIONS
----------------------------- */

function toggleNotifications() {

    appState.notificationsEnabled =
        document.getElementById(
            "notificationSwitch"
        ).checked;

    document.getElementById(
        "notificationStatus"
    ).textContent =
        appState.notificationsEnabled
            ? "Включены"
            : "Выключены";

}


/* -----------------------------
   INFO
----------------------------- */

function showInfo() {

    tg.showPopup({

        title: "RFL Rewards",

        message:
            "Бот напоминает тебе каждый день зайти в RFL и забрать ежедневную награду.\n\n" +
            "Забрал награду — бот больше не напоминает до следующего дня.\n\n" +
            "Поддерживай серию и не пропускай награды 🔥",

        buttons: [
            {
                id: "ok",
                type: "ok"
            }
        ]

    });

}


/* -----------------------------
   REWARD BOT
----------------------------- */

function openRewardBot() {

    tg.openTelegramLink(
        "https://t.me/rfl_pro_bot"
    );

}