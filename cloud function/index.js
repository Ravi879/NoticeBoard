//use below code to create cloud function, which is deployed by using firebase sdk

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


exports.noticeOnCreate = functions.database.ref('/notice/{department}/{designation}/{noticeId}').onCreate((snapshot, context) => {
    console.log("Notice onCreate");

    const notice = snapshot.val();
    const noticeId = context.params.noticeId;

    const payload = getPayload(noticeId, notice, context.params, false);
    const topic = getTopic(context.params);

    const issuerId = payload.data.issuerId.toString();
    return getOtherDetails(issuerId, topic, payload);

});

exports.noticeOnUpdate = functions.database.ref('/notice/{department}/{designation}/{noticeId}').onUpdate((change, context) => {
    console.log("Notice onUpdate");

    const after = change.after.val();
    const noticeId = context.params.noticeId;

    const payload = getPayload(noticeId, after, context.params, true);

    const topic = getTopic(context.params);

    const issuerId = payload.data.issuerId.toString();
    return getOtherDetails(issuerId, topic, payload);

});

const Departments = {
    "Mechanical": "Mechanical",
    "Civil": "Civil",
    "Electrical": "Electrical",
    "Office": "Office",
    "Library": "Library",
    "Tpo": "Tpo",
    "Gymkhana": "Gymkhana",
    "Other": "Other"
};

const DepartmentsId = {
    "Mechanical": "100",
    "Civil": "200",
    "Electrical": "300",
    "Office": "400",
    "Library": "500",
    "Tpo": "600",
    "Gymkhana": "700",
    "Other": "1100"
};

function getTopic(params) {
    let topic;
    const department = params.department;

    if (department === Departments.Mechanical || department === Departments.Civil || department === Departments.Electrical
    || department === Departments.Library || department === Departments.Tpo || department === Departments.Gymkhana) {
        topic = department;
    } else if (department === Departments.Office) {
        const designation = params.designation;
        topic = department + "-" + designation;
    } else {
        return null;
    }

    return topic;
}

function getPayload(noticeId, notice, params, isNoticeUpdate) {
    return {
        data: {
            "title": notice.title.toString(),
            "description": notice.description.toString(),
            "fbNoticeId": noticeId.toString(),
            "issueDate": notice.issueDate.toString(),
            "issuerId": notice.issuerId.toString(),
            "lastDate": notice.lastDate.toString(),
            "isNoticeUpdate": isNoticeUpdate.toString(),
            "department": params.department,
            "designation": params.designation,

            "groupName": getGroupName(params.department),
            "groupId": getGroupId(params.department)
        }
    };
}

function getGroupName(department) {
    if (department in Departments)
        return department;
    else
        return Departments.Other;
}

function getGroupId(department) {

    if (department in Departments)
        return DepartmentsId[department];
    else
        return DepartmentsId[Other];
}


function getOtherDetails(issuerId, topic, payload) {
    let issuerReference;
    return admin.database().ref("/userIndex/" + issuerId).once('value').then((snapshot) => {
        issuerReference = snapshot.val().toString();
        const reference = admin.database().ref("/" + issuerReference + "/name").once('value');
        return Promise.all([reference]);
    }).then(name => {
        payload.data.issuerName = name[0].val().toString();
        return admin.messaging().sendToTopic(topic, payload);
    }).catch(error => {
        console.log("Error........ getOtherDetails() " + error);
    });
}
