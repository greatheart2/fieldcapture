var now = ISODate();
var setting = db.setting.findOne({key:'meritaccessexpiry.warning.email.subject'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritaccessexpiry.warning.email.subject'
    }
}
setting.lastUpdated = now;
setting.value = 'Access to MERIT has been removed';
setting.description = 'The subject of the email to send when access has been removed due to no login to MERIT for a specified time'
db.setting.save(setting);


setting = db.setting.findOne({key:'meritaccessexpiry.warning.email.body'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritaccessexpiry.warning.email.body'
    }
}
setting.lastUpdated = now;
setting.value = 'Dear MERIT User\n\nOur records indicate that you have not logged into MERIT (https://fieldcapture.ala.org.au/) for nearly two years.\n\nYour access will be removed 2 weeks from today, unless you take the following action:\n\nIf you wish to retain your access at your current level, all you need to do is log into MERIT within two weeks of the date of this email.\n\nIf you no longer require access, you do not need to do anything. Your access will be automatically removed two weeks from today.\n\nIf you require access at a future stage, or miss the two week deadline, please contact the MERIT Team (merit@awe.gov.au) and we can assist you.\n\nRegards\nThe MERIT Team\nDepartment of Agriculture, Water and the Environment\nmerit@awe.gov.au\nhttps://fieldcapture.ala.org.au/';
setting.description = 'The body of the email to send when access has been removed due to no login to MERIT for a specified time';
db.setting.save(setting);





setting = db.setting.findOne({key:'meritaccessexpiry.expired.email.subject'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritaccessexpiry.expired.email.subject'
    }
}
setting.lastUpdated = now;
setting.value = 'Access to MERIT';
setting.description = 'The subject of the email to send when access has been removed due to no login to MERIT for a specified time';
db.setting.save(setting);


setting = db.setting.findOne({key:'meritaccessexpiry.expired.email.body'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritaccessexpiry.expired.email.body'
    }
}
setting.lastUpdated = now;
setting.value = 'Dear MERIT User\n\nFurther to our previous email, your access to MERIT (https://fieldcapture.ala.org.au/) has now been removed.\nIf you feel this has been done in error, or require access at a future stage, please contact the MERIT Team (merit@awe.gov.au) and we can assist you..\n\nRegards\nThe MERIT Team\nDepartment of Agriculture, Water and the Environment\nmerit@awe.gov.au\nhttps://fieldcapture.ala.org.au/';
setting.description = 'The body of the email to send when access has been removed due to no login to MERIT for a specified time';
db.setting.save(setting);


setting = db.setting.findOne({key:'meritpermissionexpiry.expired.email.subject'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritpermissionexpiry.expired.email.subject'
    }
}
setting.lastUpdated = now;
setting.value = 'Access to MERIT';
setting.description = 'The subject of the email to send when a role has been removed due to it expiring';
db.setting.save(setting);


setting = db.setting.findOne({key:'meritpermissionexpiry.expired.email.body'});
if (!setting) {
    setting = {
        dateCreated:now,
        key:'meritpermissionexpiry.expired.email.body'
    }
}
setting.lastUpdated = now;
setting.value = 'Dear MERIT User\n\nYour elevated access to MERIT (https://fieldcapture.ala.org.au/) has now been removed.\nIf you feel this has been done in error, or require access at a future stage, please contact the MERIT Team (merit@awe.gov.au) and we can assist you..\n\nRegards\nThe MERIT Team\nDepartment of Agriculture, Water and the Environment\nmerit@awe.gov.au\nhttps://fieldcapture.ala.org.au/';
setting.description = 'The subject of the email to send when a role has been removed due to it expiring';
db.setting.save(setting);
