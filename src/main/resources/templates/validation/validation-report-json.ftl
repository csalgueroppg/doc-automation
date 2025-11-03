<#-- JSON format validation report template -->
{
    "status": "<#if valid>VALID<#else>INVALID</#if>",
    "schemaVersion": <#if schemaVersion??>"${schemaVersion}"<#else>null</#if>,
    "summary": {
        "errorCount":
    },
    <#if metrics??>
    </#if>