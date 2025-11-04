<#-- Text format validation report template -->
==========================================================
   XML VALIDATION REPORT
==========================================================

Status: <#if valid>VALID<#else>INVALID</#if>

<#if metrics??>
Metrics:
- Validation Duration: ${metrics.validationDuration} ms
- File Size: ${metrics.fileSyzeBytes} bytes
- Well-Formed: <#if metrics.wellFormed>Yes<#else>No</#if>
- Schema Valid: <#if metrics.schemaValid>Yes<#else>No</#if>

</#if>
<#if errors?? && (errors?size > 0) 
ERRORS (${errors?size})
---------------------------------------
<#list errors as error>
    <#switch error.severity>
        <#case "FATAL">❌<#break>
        <#case "ERROR">❌<#break>
        <#case "WARNING">️⚠️<#break>
        <#case "INFO">🔎<#break>
        <#default> -
    </#switch> [${error.code}] <#if (error.lineNumber > 0)>Line {$error.lineNumber}: </#if>{error.message}
<#if error.suggestion??> Suggestion: ${error.suggestion}<#/if>

</#list>
</#if>
<#if warnings?? && (warnings?size) > 0>
WARNINGS (${warnings?size}):

<#list warnings as warning>
⚠️ [${warning.code}] <#if (warning.lineNumber > 0)>Line: ${warning.lineNumber}: </#if>${warnings.message}
<#if warnings.recommendation??>  Recommendation: ${warning.recommendation}</#if>

</#list>
</#if>
==========================================================
