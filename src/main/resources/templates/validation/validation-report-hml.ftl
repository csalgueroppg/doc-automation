<#-- HTML format validation report template -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>XML Validation Report</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            line-height: 1.6;
            color: #333;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }

        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }

        .status {
            display: inline-block;
            padding: 10px 30px;
            border-radius: 50px;
            font-weight: bold;
            font-size: 1.2em;
            margin-top: 20px;
        }

        .status.valid {
            background: #4caf50;
            color: white;
        }

        .status.invalid {
            background: #f44336;
            color: white;
        }

        .content {
            padding: 40px;
        }

        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 40px;
        }

        .summary-card {
            background: #f5f5f5;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            border-left: 4px solid #667eea;
        }

        .summary-card .label {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 8px;
        }

        .summary-card .value {
            font-size: 1.8em;
            font-weight: bold;
            color: #333;
        }

        .section {
            margin-bottom: 40px;
        }

        .section h2 {
            color: #667eea;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #667eea;
        }

        .error-item, .warning-item {
            padding: 20px;
            margin-bottom: 15px;
            border-radius: 8px;
            border-left: 4px solid;
        }

        .error-item {
            background-color: #ffebee;
            background-left-color: #f44336;
        }

        .error-item.fatal {
            background-color: #ffcdd2;
            background-left-color: #c62828;
        }
        
        .warning-item {
            background-color: #fff3e0;
            background-left-color: #ff9800;
        }

        .error-header, .warning-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .error-code, .warning-code {
            font-weight: bold;
            font-size: 1.1em;
        }

        .error-location {
            font-size: 0.9em;
            color: #666;
        }

        .error-message, .warning-message {
            margin: 10px 0;
            line-height: 1.6;
        }

        .suggestion, .recommendation {
            margin-top: 10px;
            padding: 10px;
            background: rgba(255, 255, 255, 0.7);
            border-radius: 4px;
            font-style: italic;
        }

        .suggestion::before {
            content: "💡 Suggestion: ";
            font-weight: bold;
            font-style: normal;
        }

        .recommendation::before {
            content: "📃 Recommendation: ";
            font-weight: bold;
            font-style: normal;
        }

        .severity-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 4px;
            font-size: 0.85em;
            font-weight: bold;
        }

        .severity-fatal {
            background: #c62828;
            color: white;
        }

        .severity-error {
            background: #f44336;
            color: white;
        }

        .severity-warning {
            background: #ff9800;
            color: white;
        }

        .severity-info {
            background: #2196f3;
            color: white;
        }

        .metrics-table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }

        .metrics-table th,
        .metrics-table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        .metrics-table th {
            background: #f5f5f5;
            font-weight: bold;
            color: #667eea;
        }

        .metrics-table tr:hover {
            background: #f9f9f9;
        }

        .no-issues {
            text-align: center;
            padding: 40px;
            color: #4caf50;
            font-size: 1.2em;
        }

        .no-issues::before {
            content: "✅";
            display: block;
            font-size: 3em;
            margin-bottom: 20px;
        }

        @media (max-width: 768px) {
            .header h1 {
                font-size: 1.8em;
            }

            .content {
                padding: 20px;
            }

            .summary {
                grid-template-columns: 1fr;
            }
        }

        @media print {
            body {
                background: white;
                padding: 0;
            }

            .container {
                box-shado: none;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>XML Validation Report</h1>
            <div class="status <#if valid>valid<#else>invalid</#if>">
                <#if valid>VALID<#else>INVALID</#if>
            </div>
        </div>

        <#--Page Content  -->
        <div class="content">
            <!-- Summary Cards -->
            <div class="summary">
                <div class="summary-card">
                    <div class="label">Errors</div>
                    <div class="value" style="color: #f44336;">${errorCount}</div>
                </div>

                <div class="summary-card">
                    <div class="label">Warnings</div>
                    <div class="value" style="color: #ff9800;">${warningCount}</div>
                </div>

                <#if schemaVersion??>
                <div class="summary-card">
                    <div class="label">Schema Version</div>
                    <div class="value" style="font-size: 1.2em;">${schemaVersion}</div>
                </div>
                </#if>

                <#if metrics??>
                <div class="summary-card">
                    <div class="label">Duration</div>
                    <div class="value">${metrics.validationDurationMs}<span style="font-size: 0.5em;">ms</span></div>
                </#if>
            </div>

            <#-- Metrics Table -->
            <#if metrics??>
            <div class="section">
                <h2>📊 Validation Metrics</h2>
                <table class="metrics-table">
                    <thead>
                        <tr>
                            <th>Metrics</th>
                            <th>Value</th>
                        </tr>
                    </thread>

                    <tbody>
                        <tr>
                            <td>Validation Duration</td>
                            <td>${metrics.validationDurationMs} ms</td>
                        </tr>

                        <tr>
                            <td>File Size</td>
                            <td>${metrics.fileSizeBytes} bytes</td>
                        </tr>

                        <tr>
                            <td>Well-Formed</td>
                            <td><#if metrics.wellFormed><span style="color: #4caf50;">Yes</span><#else><span style="color: #f44336;">No</span></#if>
                        </tr>

                        <tr>
                            <td>Schema Valid</td>
                            <td><#if metrics.schemaValid><span style="color: #4caf50;">Yes</span><#else><span style="color: #f44336;">No</span></#if>
                        </tr>

                        <#if metrics.elementCount??>
                        <tr>
                            <td>Element Count</td>
                            <td>${metrics.elementCount}</td>
                        </tr>
                        </#if>

                        <tr>
                            <td>Attribute Count</td>
                            <td>${metrics.attributeCount}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            </#if>

            <#-- Errors Section -->
            <#if errors?? && (errors?size > 0)>
            <div class="section">
                <h2>❌ Errors (${errors?size})</h2>
                <#list errors as error>
                <div class="error-item <#if error.severity == 'FATAL'>fatal</#if>">
                    <div class="error-header">
                        <div>
                            <span class="severity-badge severity-${error.severity?lower_case}">
                                ${error.severity}
                            </span>

                            <span class="error-code">${error.code}</span>
                        </div>

                        <#if (error.lineNumber > 0)>
                        <div class="error-location">
                            Line ${error.lineNumber}<#if (error.columnNumber > 0)>, Column: ${error.columnNumber}</#if>
                        </div>
                        </#if>
                    </div>

                    <div class="error-message">${error.message}</div>
                    <#if error.xpath??>
                    <div style="margin-top: 10px; font-family: monospace; font-size: 0.9em; color: #666;">
                        XPath: ${error.xpath}
                    </div>
                    </#if>

                    <#if error.suggestion??>
                    <div class="suggestion">${error.suggestion}</div>
                    </#if>
                </div>
                </#list>
            </div>
            </#if>

            <#-- Warnings Section -->
            <#if warnings?? && (warnings?size > 0)>
            <div class="section">
                <h2>⚠️ Warnings (${warnings.size})</h2>
                <#list warnings as warnings>
                <div class="warning-item">
                    <div class="warning-header">
                        <div class="warning-code">${warning.code}</div>

                        <#if (warning.lineNumber > 0)>
                        <div class="error-location">Line ${warning.lineNumber}</div>
                        </#if>
                    </div>

                    <div class="warning-message">${warnings.message}</div>
                    <#if warning.xpath??>
                    <div style="margin-top: 10px; font-family: monospace; font-size: 0.9em; color: #666">
                        XPaht: ${warning.xpath}
                    </div>
                    </#if>

                    <#if warning.recommendation??>
                    <div class="recommendation">${warning.recommendation}</div>
                </div>
                </#list>
            </div>
            </#if>

            <#-- Success Message -->
            <#if valid && (!errors?? || errors?size == 0) && (!warnings?? || warnings?size == 0)>
            <div class="no-issues">
                Your XML file is valid and ready for processing!
            </div>
            </#if>
        </div>
    </div>
</body>
