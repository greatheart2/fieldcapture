package au.org.ala.fieldcapture

import pages.RlpProjectPage

class DatasetSpec extends StubbedCasSpec{

    def setup(){
        useDataSet("dataset3")
    }

    def cleanup() {
        logout(browser)
    }

    def "Add new data set in to project"() {
        setup:
        String projectId = 'fdFundProject'
        login([userId: '1', role: "ROLE_FC_ADMIN", email: 'admin@nowhere.com', firstName: "MERIT", lastName: 'USER'], browser)

        when:
        to RlpProjectPage, projectId

        then:
        waitFor { at RlpProjectPage }

        when:
       // def dataSet = openAndEditDataSet()
9
        datasetTab.click()
        waitFor {datasetDetails.displayed}
        datasetDetails.addNewDataset.click()
        waitFor{datasetDetails.datasetContent.displayed}
        def dataSet = datasetDetails.datasetContent
        dataSet.title = "Title"
        dataSet.programOutcome = "5. By 2023, there is an increase in the awareness and adoption of land management practices that improve and protect the condition of soil, biodiversity and vegetation."
        dataSet.investmentPriority = ["Soil acidification"]
        dataSet.type = "Baseline dataset associated with a project outcome"
        dataSet.measurementTypes = ["Soil erosion"]
        dataSet.methods = ["Genetic sampling", "Area sampling"]
        dataSet.methodDescription = "Method description"
        dataSet.collectionApp = "Collection App"
        dataSet.location = "Location"
        dataSet.startDate ="21-01-2021"
        dataSet.endDate ="21-01-2022"
        dataSet.addition = "Yes"
        dataSet.collectorType = "University researcher"
        dataSet.qa = "Yes"
        dataSet.published = "Yes"
        dataSet.storageType = "Aurion"
        dataSet.publicationUrl = "url"
        dataSet.format = "JSON"
        dataSet.sensitivities =["Commercially sensitive", "Ecologically sensitive"]
        dataSet.dataOwner = "data owner"
        dataSet.custodian = "custodian"

        dataSet.createButton.click()

      //  to RlpProjectPage, projectId

        then:
        waitFor {$("#project-data-sets .fa-edit").displayed}
        $("#project-data-sets .fa-edit").click()

        def set = datasetDetails.datasetContent
        set.title == "Title"
        set.programOutcome == "5. By 2023, there is an increase in the awareness and adoption of land management practices that improve and protect the condition of soil, biodiversity and vegetation."
        set.investmentPriority == ["Soil acidification"]
        set.type == "Baseline dataset associated with a project outcome"
        set.measurementTypes == ["Soil erosion"]
        set.methods == ["Genetic sampling", "Area sampling"]
        set.methodDescription == "Method description"
        set.collectionApp == "Collection App"
        set.location == "Location"
        set.startDate =="21-01-2021"
        set.endDate =="21-01-2022"
        set.addition == "Yes"
        set.collectorType == "University researcher"
        set.qa == "Yes"
        set.published == "Yes"
        set.storageType == "Aurion"
        set.publicationUrl == "url"
        set.format == "JSON"
        set.sensitivities ==["Commercially sensitive", "Ecologically sensitive"]
        set.dataOwner == "data owner"
        set.custodian == "custodian"



    }
}
