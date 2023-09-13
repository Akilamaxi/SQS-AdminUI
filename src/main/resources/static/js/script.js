document.addEventListener("DOMContentLoaded", function() {
    // Fetch messages when the page loads
    const urlParams = new URLSearchParams(window.location.search);
    const queueUrl = urlParams.get('queueUrl');
    fetchMessages(queueUrl);
    // Call fetchMessages every 1000 milliseconds (1 second)
    setInterval(() => fetchMessages(queueUrl), 10000);
});

function fetchMessages(queueUrl) {
    const requestData = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            queueUrl: queueUrl
        })
    };

    fetch("/sqs/receive-message", requestData)
        .then(response => response.json())
        .then(data => {
            populateTable(data);
        })
        .catch(error => {
            console.error('There was an error fetching the SQS messages:', error);
        });
}

function populateTable(messages) {
    let tableBody = document.getElementById("messageTableBody");
    tableBody.innerHTML = ""; // Clear previous rows

    messages.forEach(message => {
        let row = tableBody.insertRow();

        let messageIdCell = row.insertCell(0);
        messageIdCell.textContent = message.messageId;

        let attributesCell = row.insertCell(1);
        attributesCell.textContent = JSON.stringify(message.attributes);  // Convert attributes to string

        let sentDateCell = row.insertCell(2);
        sentDateCell.textContent = message.sentDate;  // Assuming SentDate is a field in your message

        let receivedDateCell = row.insertCell(3);
        receivedDateCell.textContent = message.receivedDate;  // Assuming ReceivedDate is a field in your message

        let messageBodyCell = row.insertCell(4);
        let pre = document.createElement("pre");  // Use <pre> for formatted JSON view
        pre.textContent = JSON.stringify(message.body, null, 4);  // Indent for better readability
        messageBodyCell.appendChild(pre);
    });
}