~~1. Raise Indexing events (Start, End, Removed)~~
~~2. Collection Index~~
~~3. Validate indexing on null value (it should index null values)~~
3. All exception message must be detailed and unique as no number is there now.
~~5. Old nitrite data compatibility check~~
6. LuceneIndexer, test with testIssue174()
~~7. https://stackoverflow.com/questions/10936625/using-readclassdescriptor-and-maybe-resolveclass-to-permit-serialization-ver/14608062#14608062~~
~~8. Why attribute is coming as null~~
~~9. Migrate data and write to a new file~~
~~10. Make all serializable to nitriteserializable and override readObject, writeObject~~

DataGate Server:

https://stackoverflow.com/questions/42644779/how-to-secure-a-websocket-endpoint-in-java-ee
https://github.com/ls1intum/jReto

https://github.com/eranyanay/1m-go-websockets
https://www.freecodecamp.org/news/million-websockets-and-go-cc58418460bb/

IPFS:

https://www.freecodecamp.org/news/how-to-build-mongodb-like-datastore-using-interplanetary-linked-data-in-5-minutes/









title DataGate Message Protocol


Replica (R1)->Server: Connect Message
note left of Server: Validates Auth token in message

Replica (R2)->Server: DataGateFeed (r2f1)

alt Valid token
    Server->Replica (R1): ConnectAck Message
else Validation failed
    Server->Replica (R1): Error Message
    note left of Replica (R1): 
        Closes connection to server
        and will not receive any further
        communications from server
        until next successful Connect
    end note
end

Replica (R1)->Replica (R1): Find last sync time
Replica (R1)->Server: BatchChangeStart Message

Replica (R2)->Server: DataGateFeed (r2f2)

Server->Replica (R1): BatchChangeAck Message
Replica (R1)->Replica (R1): Find changes since last sync
note right of Replica (R1): 
    If changes exists sends changes 
    in chunks, size is sets in replication
    config
end note

Replica (R1)->Server: BatchChangeContinue Message

note left of Server:
    Server creates DataGateFeed message
    from BatchChangeContinue and broadcast
    to all connected peers
end note

Server-> Replica (R2): DataGateFeed message (r1f1)
Replica (R2)->Replica (R2): Save message header time as last sync time
Replica (R1)->Server: BatchChangeContinue Message

Replica (R2)->Server: DataGateFeed (r2f3)
Server-> Replica (R2): DataGateFeed message (r1f2)
Replica (R2)->Replica (R2): Save message header time as last sync time

note right of Replica (R1): 
    When there is no more changes to send
    send BatchChangeEnd with last sync time
end note
Replica (R1)->Server: BatchChangeEnd Message

Replica (R2)->Server: DataGateFeed (r2f4)

note left of Server:
    From this point on Server can send 
    DataGateFeed broadcast messages to R1.
    
    Server stores replica id for further
    feed message.
    
    Find last sync time from BatchChangeEnd 
    message
end note
Server->Server: Stores replica id
Server->Server: Find changes since last sync

note left of Server:
    Changes found are: 
    r2f1, r2f2, r2f3, r2f4
end note

Replica (R2)->Server: DataGateFeed (r2f5)
Server->Replica (R1): DataGateFeed (r2f5)
Replica (R1)->Replica (R1): Save message header time as last sync time


Server->Replica (R1): BatchChangeStart Message
Replica (R1)->Server: BatchChangeAck Message
note left of Server: If changes exists sends changes in chunks
Server->Replica (R1): BatchChangeContinue Message

Replica (R2)->Server: DataGateFeed (r2f6)
Server->Replica (R1): DataGateFeed (r2f6)
Replica (R1)->Replica (R1): Save message header time as last sync time

Server->Replica (R1): BatchChangeContinue Message
Server->Replica (R1): BatchChangeEnd Message
Replica (R1)->Replica (R1): Save message header time as last sync time

Replica (R1)->Server: RegisterFeed Message
note left of Server: Server stores replica id for further feed message


Replica (R1)->Server: DataGateFeed Message
Server->Replica (R1): DataGateFeedAck Message
Replica (R1)->Replica (R1): Save message header time as last sync time

note right of Server: Broadcast DataGateFeed message to peers
Server->Replica (R1): DataGateFeed Message
Replica (R1)->Replica (R1): Save message header time as last sync time

Replica (R1)->Server: Disconnect Message
Server->Server: Removes replica id





