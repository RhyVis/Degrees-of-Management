<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JSON Configuration Editor</title>
    <!-- JSONEditor CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/jsoneditor/10.1.3/jsoneditor.min.css" rel="stylesheet" type="text/css">
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #121212;
            color: #e0e0e0;
        }
        #container {
            display: flex;
            flex-direction: column;
            max-width: 1200px;
            margin: 0 auto;
            background-color: #1e1e1e;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.3);
        }
        #instance-selector {
            margin-bottom: 20px;
        }
        #editor-container {
            height: 500px;
            border: 1px solid #333;
            background-color: #2a2a2a;
        }
        .action-buttons {
            margin-top: 20px;
            display: flex;
            gap: 10px;
        }
        button {
            padding: 10px 15px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background-color: #45a049;
        }
        .reload-btn {
            background-color: #2196F3;
        }
        .reload-btn:hover {
            background-color: #0b7dda;
        }
        .delete-btn {
            background-color: #f44336;
        }
        .delete-btn:hover {
            background-color: #d32f2f;
        }
        .delete-btn:disabled {
            background-color: #666;
            cursor: not-allowed;
        }
        select, input {
            background-color: #2a2a2a;
            color: #e0e0e0;
            border: 1px solid #444;
            padding: 8px;
            border-radius: 4px;
        }
        select:focus, input:focus {
            outline: none;
            border-color: #2196F3;
        }
        h1 {
            color: #e0e0e0;
            border-bottom: 1px solid #444;
            padding-bottom: 10px;
        }
        /* Override JSONEditor styles for dark mode */
        .jsoneditor {
            border: 1px solid #444 !important;
            background-color: #2a2a2a !important;
        }
        .jsoneditor-menu {
            background-color: #333 !important;
            border-bottom: 1px solid #444 !important;
        }
        .jsoneditor-navigation-bar {
            background-color: #333 !important;
            border-bottom: 1px solid #444 !important;
        }
        .jsoneditor-tree, .jsoneditor-tree div {
            background-color: #2a2a2a !important;
            color: #e0e0e0 !important;
        }
        .jsoneditor-field, .jsoneditor-value {
            color: #e0e0e0 !important;
        }
        .jsoneditor-field[contenteditable=true]:focus,
        .jsoneditor-value[contenteditable=true]:focus {
            background-color: #3a3a3a !important;
            border: 1px solid #2196F3 !important;
        }
        .jsoneditor-schema-error {
            background-color: rgba(255, 76, 76, 0.2) !important;
        }
    </style>
</head>
<body>
    <div id="container">
        <h1>Instance Editor</h1>

        <div id="instance-selector">
            <label for="instance-select">Select Instance:</label>
            <select id="instance-select">
                <option value="">New Instance</option>
                <#list instances?keys as instanceId>
                    <option value="${instanceId}">${instanceId}</option>
                </#list>
            </select>
            <button id="new-instance-button" style="margin-left: 10px;">New Instance</button>
        </div>

        <div id="editor-container"></div>

        <div class="action-buttons">
            <button id="save-button">Save Changes</button>
            <button id="delete-button" class="delete-btn" disabled>Delete Instance</button>
            <button class="reload-btn" id="reload-button">Reload All Configurations</button>
            <button id="back-button">Back to Dashboard</button>
        </div>
    </div>

    <!-- JSONEditor JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jsoneditor/10.1.3/jsoneditor.min.js"></script>
    <script>
        // Initialize variables
        let editor;
        let currentInstanceId = '';

        // Initialize JSONEditor
        document.addEventListener('DOMContentLoaded', () => {
            const container = document.getElementById('editor-container');
            const schema = {
            type: 'object',
            properties: {
                id: {
                    type: 'string',
                    description: 'Unique identifier for the instance'
                },
                name: {
                    type: 'string',
                    description: 'Display name of the instance'
                },
                foundation: {
                    type: 'string',
                    description: 'Base game for this instance'
                },
                layers: {
                    type: 'array',
                    items: { type: 'string' },
                    description: 'Game layers to apply'
                },
                mods: {
                    type: 'array',
                    items: { type: 'string' },
                    description: 'Mods to apply to this instance'
                }
            },
            required: ['id', 'name', 'foundation']
            };
            const options = {
                mode: 'tree',
                modes: ['tree', 'view', 'form', 'code', 'text'],
                schema: schema,
                schemaRefs: {},
                onError: (err) => {
                    alert('JSON Error: ' + err.toString());
                }
            };

            editor = new JSONEditor(container, options);
            editor.set({});
        });

        // Instance selector change event
        document.getElementById('instance-select').addEventListener('change', (e) => {
            const instanceId = e.target.value;
            const deleteButton = document.getElementById('delete-button');

            if (!instanceId) {
                editor.set({});
                currentInstanceId = '';
                deleteButton.disabled = true;
                return;
            }

            currentInstanceId = instanceId;
            deleteButton.disabled = false;

            // Fetch the configuration
            fetch("/edit/registry/" + instanceId)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    editor.set(data);
                })
                .catch(error => {
                    console.error('Error fetching configuration:', error);
                    alert('Failed to load configuration: ' + error.message);
                });
        });

        // Add new
        document.getElementById('new-instance-button').addEventListener('click', () => {
            currentInstanceId = '';
            document.getElementById('instance-select').value = '';

            // Reset editor with template
            editor.set({
                id: "",
                name: "",
                foundation: "",
                layers: [],
                mods: []
            });
        });

        // Save button click event
        document.getElementById('save-button').addEventListener('click', () => {
            try {
                const errors = editor.validate()
                if (errors.length > 0) {
                    let errorMessage = 'Validation errors:\n';
                    errors.forEach(error => {
                       errorMessage += '- ' + error.path + ': ' + error.message + '\n';
                    });
                    alert(errorMessage);
                    return;
                }

                const jsonData = editor.get();

                if (!jsonData.id || jsonData.id.trim() === '') {
                    alert('Instance ID cannot be empty');
                    return;
                }

                fetch('/edit/update', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(jsonData)
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.text();
                })
                .then(_ => {
                    alert('Configuration saved successfully!');
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error saving configuration:', error);
                    alert('Failed to save configuration: ' + error.message);
                });
            } catch (e) {
                alert('Invalid JSON: ' + e.message);
            }
        });

        document.getElementById('delete-button').addEventListener('click', () => {
            if (!currentInstanceId) {
                alert('No instance selected!');
                return;
            }

            if (confirm('Are you sure you want to delete this instance?')) {
                fetch('/edit/registry/' + currentInstanceId, {
                    method: 'DELETE'
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    alert('Instance deleted successfully!');
                    window.location.reload();
                })
                .catch(error => {
                    console.error('Error deleting instance:', error);
                    alert('Failed to delete instance: ' + error.message);
                });
            }
        });

        // Reload button click event
        document.getElementById('reload-button').addEventListener('click', () => {
            fetch('/edit/reload/registry', {
                method: 'POST',
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                window.location.reload();
            })
            .catch(error => {
                console.error('Error reloading configurations:', error);
                alert('Failed to reload configurations: ' + error.message);
            });
        });

        // Back button click event
        document.getElementById('back-button').addEventListener('click', () => {
            window.location.href = '/';
        });
    </script>
</body>
</html>
