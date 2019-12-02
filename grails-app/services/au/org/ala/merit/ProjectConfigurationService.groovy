package au.org.ala.merit

/**
 * The ProjectConfigurationService is responsible for obtaining the configuration used to present a project in MERIT.
 *
 * The configuration specifies things like the template used to render the project page, the type and frequency of
 * project reporting and how activity / report data entry behaves.
 *
 * It is mostly based on the program under which the project is being run.
 */
class ProjectConfigurationService {

    ProgramService programService
    MetadataService metadataService
    ManagementUnitService managementUnitService

    Map getProjectConfiguration(Map project) {
        Map programConfig

        if (project.programId) {
            programConfig = buildConfigFromProgram(project)
        }
        else {
            programConfig = buildDefaultConfig(project)
        }

        programConfig.autogeneratedActivities = hasAutogeneratedActivities(programConfig)
        programConfig
    }

    private Map buildConfigFromProgram(Map project) {
        Map program = programService.get(project.programId)
        Map programConfig = new ProgramConfig(program.inheritedConfig ?: [:])
        programConfig.services = metadataService.getProjectServices()
        // Outcomes are defined by the program
        programConfig.outcomes = program.outcomes ?: []
        programConfig.priorities = program.priorities ?: []
        programConfig.themes = program.themes ?: []
        programConfig.program = program

        // The project configuration is mostly derived from the program it is run
        // under, but if it is delivered by the management unit arrangement, some
        // some of the configuration can be supplied by the management unit.
        if (project.managementUnitId) {
            Map managementUnit = managementUnitService.get(project.managementUnitId)
            Map config = managementUnit.config

            // There may be only a subset of the program assets/priorities that
            // actually exist within the boundary of the management unit.
            // (e.g. threatened species may be known to exist within some
            // management units but not others)
            if (managementUnit.priorities) {
                programConfig.priorities = managementUnit.priorities
            }
            // If the program doesn't define outcomes, use ones for the management unit.
            if (!programConfig.outcomes && managementUnit.outcomes) {
                programConfig.outcomes = managementUnit.outcomes
            }

            // Allow management units to override project reporting frequency or include additional reports
            // to those defined by the program configuration.
            List extraReports = []
            if (!programConfig.projectReports) {
                programConfig.projectReports = []
            }
            config.projectReports?.each { Map configuration ->
                Map programReport = programConfig.projectReports?.find {
                    return it.category == configuration.category && it.activityType == configuration.activityType
                }
                if (programReport) {
                    programReport.reportingPeriodInMonths = configuration.reportingPeriodInMonths
                }
                else {
                    extraReports << configuration
                }
            }
            programConfig.projectReports?.addAll(extraReports)
        }
        programConfig
    }

    /**
     * This creates a configuration from the legacy MERIT programsModel.
     */
    private Map buildDefaultConfig(Map project) {
        Map config = metadataService.getProgramConfiguration(project.associatedProgram, project.associatedSubProgram)
        Map programConfig = new ProgramConfig(config)
        programConfig.activityBasedReporting = true

        // Default configuration for project stage reports.

        Integer reportingPeriodInMonths = 6 // Default period
        try {
            reportingPeriodInMonths = Integer.parseInt(programConfig.reportingPeriod)
        }
        catch (Exception e) {
            log.warn("Invalid period specified in program: "+programConfig.reportingPeriod)
        }

        programConfig.projectReports = [
                [
                        weekDaysToCompleteReport:programConfig.weekDaysToCompleteReport,
                        reportType:ReportService.REPORT_TYPE_STAGE_REPORT,
                        reportingPeriodInMonths: reportingPeriodInMonths,
                        reportsAlignedToCalendar: Boolean.valueOf(programConfig.reportingPeriodAlignedToCalendar),
                        reportNameFormat: "Stage %1d",
                        reportDescriptionFormat: "Stage %1d for ${project.name}"
                ]
        ]
        programConfig
    }

    /**
     * A configuration consists of autogenerated activities if every report configuration specifies an activityType.
     * (Specifying an activity type will result in an activity being auto-generated for each report type).
     * @param config the project configuration to check.
     */
    private boolean hasAutogeneratedActivities(Map config) {

        boolean autogeneratedActivities = false
        if (config.projectReports) {
            autogeneratedActivities = config.projectReports.every{it.activityType != null}
        }

        autogeneratedActivities
    }
}
