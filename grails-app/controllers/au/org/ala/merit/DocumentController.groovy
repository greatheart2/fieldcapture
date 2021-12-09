package au.org.ala.merit

import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.json.JsonSlurper
import org.apache.http.HttpStatus

import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK;

class DocumentController {

    static allowedMethods = [bulkUpdate: 'POST', documentUpdate: 'POST', deleteDocument: 'POST']

    static final String DOCUMENT_DOWNLOAD_PATH = '/document/download/'

    DocumentService documentService
    WebService webService
    GrailsApplication grailsApplication

    def index() {}

    def createLink() {
        def link = request.JSON
        documentService.saveLink(link)
    }

    @PreAuthorise(accessLevel = "siteAdmin")
    def bulkUpdate() {
        def result = [:]
        def documents = request.JSON
        if (!documents || !documents instanceof List) {
            response.status = SC_BAD_REQUEST
            result.error = 'Request body must contain a json array of documents to update'
        }
        else {

            documents.each {
                def resp = documentService.updateDocument(it)
                if (resp.statusCode != SC_OK || result.error) {
                    response.staus = resp.statusCode
                    result.error = 'There was an error performing the update - please try again later'
                }
            }

        }
        render result as JSON
    }

    /**
     * Proxies to the ecodata document controller.
     * @param id the id of the document to update (if not supplied, a create operation will be assumed).
     * @return the result of the update.
     */
    def documentUpdate(String id) {

        if (request.respondsTo('getFile')) {
            def f = request.getFile('files')
            def originalFilename = f.getOriginalFilename()
            if(originalFilename){
                Map document = new JsonSlurper().parseText(params.document)
                if (id) {
                    document.documentId = id
                }
                Map result = documentService.updateDocument(document, f.originalFilename, f.contentType, f.inputStream)
                if (result.statusCode == SC_OK) {
                    if (result.content) {
                        result = result.content
                    }
                    render result as JSON
                }
                else {
                    response.setStatus(result.statusCode)
                    render result as JSON
                }
            } else {
                //flag error for extension
                response.setStatus(SC_BAD_REQUEST)
                def error = [error: "Unable to retrieve the file name.",
                             statusCode: "400",
                             detail: "Unable to retrieve the file name."] as JSON
                render error as JSON
            }
        } else {
            // This is returned to the browswer as a text response due to workaround the warning
            // displayed by IE8/9 when JSON is returned from an iframe submit.
            def result = documentService.updateDocument(JSON.parse(params.document))
            render result as JSON
        }
    }

    /**
     * Proxies to the eco data document controller to delete the document with the supplied id.
     * @param id the id of the document to delete.
     * @return the result of the deletion.
     */
    def deleteDocument(String id) {
        def responseCode = documentService.delete(id)
        render status: responseCode
    }

    def download(String path, String filename)  {
        final String THUMBNAIL_PREFIX = "thumb_"
        if (filename) {
            String originalName = filename
            if (filename.startsWith(THUMBNAIL_PREFIX)) {
                originalName = filename.substring(THUMBNAIL_PREFIX.length())
            }
            Map results = documentService.search(filepath: path, filename: originalName)
            if (results && results.documents) {
                Map document = results.documents[0]

                if (documentService.canView(document)) {
                    String url = buildDownloadUrl(path, filename)
                    webService.proxyGetRequest(response, url, false, false)
                    return null
                }
            }
        }
        response.status = HttpStatus.SC_NOT_FOUND
    }

    private String buildDownloadUrl(String path, String filename) {
        String url = grailsApplication.config.ecodata.baseUrl + DOCUMENT_DOWNLOAD_PATH
        if (path) {
            url += path + '/'
        }
        url += filename

        url
    }
}
