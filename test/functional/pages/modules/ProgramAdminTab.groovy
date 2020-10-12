package pages.modules

import geb.Module

class ProgramAdminTab extends Module {

    static content = {
        editTab {$('#edit-program-details-tab')}
        editButton(required:false) {$('a.editBtnAction')}
        addSubProgramButton(required: false) {$('a.addSubProgramButton')}
    }



}
