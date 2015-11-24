modules = {
    merit_projects {
        dependsOn 'projects'
        resource url:'js/meriplan.js'
        resource url:'js/risks.js'
        resource url:'css/project.css'
    }

    greenArmy {
        defaultBundle 'application'
        resource 'js/greenArmyReporting.js'
    }


    admin {
        dependsOn 'application'
        resource url: 'js/admin.js'
    }

    newSkin {
        dependsOn 'nrmSkin'
        resource url: 'css/global-styles.css'
    }


}