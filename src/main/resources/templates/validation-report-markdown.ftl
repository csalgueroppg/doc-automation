<#-- Markdown format validation report template -->
# XML Validation Report

## Summary

- **Status**: <#if valid>✅ Valid<#else>❌ Invalid</#if>
<#if schemaVersion??>- **Schema Version**: ${schemaVersion}</#if>
- **Errors**: ${errorCount}
- **Warnings**: ${warningCount}

<#if metrics??>
## Metrics

| Metric | Value |
|--------|-------|
| Validation Duration | ${metrics.validationDurationMs} ms |
| File Size | ${metrics.fileSizeBytes} bytes |
| Well-Formed | <#if metrics.wellFormed>✅ Yes<#else>❌No</#if> |
| Schema Valid | <#if metrics.schemaValid>✅ Yes<#else>❌No</#if> |

</#if>
<#if errors?? && (errors?size > 0)>
## Errors

<#list errors as error>
### ${error.severity}: ${error.code}

<#if (error.lineNumber > 0)>
**Location**: Line {$error.lineNumber}<#if (error.columnNumber > 0)>, Column ${error.columnNumber}</#if>

</#if>
**Message**: ${error.message}

<#if error.xpath??>**XPath**: `${error.xpath}`

</#if>
<#if error.suggestion??>
**Suggestion**: ${error.suggestion}

</#if>
---

</#list>
</#if>
<#if warnings?? && (warnings?size > 0)>
## Warnings

<#list warnings as warning>
### ⚠️ ${warning.code}

**Message**: ${warning.message}

<#if warning.xpath??>**XPath**: `${warning.xpath}`

</#if>
<#if warning.recommended>
**Recommendation**: ${warning.recommendation}

</#if>
---

</#list>
</#if>
<#if valid>
## ✅ Validation Passed

Your XML file is valid and ready for processing!
<#else>
## ❌ Validation Failed 

Please fix the errors above before processing.
</#if>