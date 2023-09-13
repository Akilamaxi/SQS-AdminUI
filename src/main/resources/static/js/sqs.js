var socket = new SockJS('/ws-endpoint');
var stompClient = Stomp.over(socket);
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/queue/received', function (message) {
        showAlert('Received: ' + message.body);
    });
});

// Call listQueues() when the page loads.
document.addEventListener('DOMContentLoaded', (event) => {
    listQueues();
});

function showAlert(message, type = 'success') {
    let alertHTML = `
                <div class="alert alert-${type} alert-dismissible fade show mt-3" role="alert">
                    ${message}
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
            `;

    const container = document.getElementById('alertContainer');
    container.innerHTML = alertHTML;

    // Auto-remove the alert after 5 seconds
    setTimeout(() => {
        $(container).find('.alert').alert('close');
    }, 5000);
}

function requestMessages(queueUrl) {
    stompClient.send("/app/receiveMessage", {}, queueUrl);
}

function setQueueURL(queueUrl) {
    document.getElementById('modalQueueUrl').value = queueUrl;
}

function addAttributeRow() {
    var attributesContainer = document.getElementById('attributesContainer');
    var newRow = document.querySelector('.attribute-row').cloneNode(true);
    attributesContainer.appendChild(newRow);
}

function removeAttributeRow(button) {
    var row = button.parentElement.parentElement;
    if (document.querySelectorAll('.attribute-row').length > 1) { // Ensure there's always at least one row
        row.remove();
    } else {
        // Clear the values if it's the last row
        row.querySelector('input[name="attributeName[]"]').value = '';
        row.querySelector('input[name="attributeValue[]"]').value = '';
        row.querySelector('select[name="attributeDataType[]"]').value = 'String';
    }
}

function prepareReceiveMessage(queueUrl) {
    fetch(`/receive-message?queueUrl=${encodeURIComponent(queueUrl)}`)
        .then(response => response.json())
        .then(data => {
            let messageContent = '';
            data.forEach(message => {
                messageContent += `<p>${message.body}</p>`;
            });
            document.getElementById('receivedMessagesBody').innerHTML = messageContent;
        });
}

function createQueue() {
    let queueName = document.getElementById("queueName").value;
    let isFifo = document.getElementById("isFifo").checked;

    fetch('/sqs/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({queueName, isFifo})
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Queue created successfully
                showAlert('Queue created successfully.');
                document.getElementById('createQueueForm').reset();
                // Instead of listing all queues again, just add the new queue
                addQueueRow(data.queueUrl);

            } else {
                showAlert('Error creating queue.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error creating queue.');
        });
}

function addQueueRow(queue) {
    let queueList = document.getElementById('queueList');
    let templateRow = document.getElementById('queueRowTemplate');
    let clonedRow = templateRow.cloneNode(true);

    clonedRow.removeAttribute('id');
    clonedRow.style.display = '';  // make it visible
    let buttons = clonedRow.querySelectorAll('button');
    buttons.forEach(button => {
        button.setAttribute('data-queueurl', queue);
    });
    clonedRow.querySelector('.queueUrl').textContent = queue;
    queueList.appendChild(clonedRow);
}

function listQueues() {

    fetch('/sqs/list')
        .then(response => response.json())
        .then(queues => {
            let queueList = document.getElementById('queueList');
            let templateRow = document.getElementById('queueRowTemplate');

            queues.forEach(queue => {
                let clonedRow = templateRow.cloneNode(true);
                clonedRow.removeAttribute('id');
                clonedRow.style.display = '';  // make it visible

                clonedRow.querySelector('.queueUrl').textContent = queue;
                let buttons = clonedRow.querySelectorAll('button');
                buttons.forEach(button => {
                    button.setAttribute('data-queueurl', queue);
                });


                queueList.appendChild(clonedRow);
            });
        });
}

function openMessagePolling(queueUrl) {
    window.open('message-polling.html?queueUrl=' + encodeURIComponent(queueUrl), '_blank','width=600,height=400');
}

function deleteQueue(queueUrl,buttonElement) {
    fetch('/sqs/delete-queue', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({queueUrl})
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Queue deleted successfully.');
                // Remove the corresponding row from the table
                let parentRow = buttonElement.closest('tr');
                parentRow.remove();
            } else {
                showAlert('Error deleting queue.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error deleting queue.');
        });
}
document.querySelector('form[action="/send-message"]').addEventListener('submit', function(event) {
    event.preventDefault();  // Prevents the form from submitting the traditional way

    const formData = new FormData(event.target);
    const data = {
        queueUrl: formData.get('queueUrl'),
        message: formData.get('message'),
        deduplicationId: formData.get('deduplicationId'),
        groupId: formData.get('groupId'),
        attributes: []
    };

    // Collecting attributes
    const attributeNames = formData.getAll('attributeName[]');
    const attributeValues = formData.getAll('attributeValue[]');
    const attributeDataTypes = formData.getAll('attributeDataType[]');

    for (let i = 0; i < attributeNames.length; i++) {
        if (attributeNames[i] && attributeValues[i] && attributeDataTypes[i]) {  // Ensures no empty attributes
            data.attributes.push({
                name: attributeNames[i],
                value: attributeValues[i],
                dataType: attributeDataTypes[i]
            });
        }
    }

    // AJAX call using fetch
    fetch('/sqs/send-message', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(json => {
            if (json.success) {
                showAlert('Message sent successfully.');
                event.target.reset();  // Clear the form
                $('#sendMessageModal').modal('hide');
            } else {
                showAlert('Error sending message.');
            }
        })
        .catch(err => {
            console.error('Error:', err);
            showAlert('Error sending message.');
        });
});

