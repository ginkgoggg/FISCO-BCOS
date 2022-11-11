pragma solidity ^0.5.2;
 
// import "./Table.sol";
 
contract LogInfo{
 
	address private _owner;
 
	modifier onlyOwner{
		require(_owner == msg.sender, "Auth: only owner is authorized");
		_;
	}
 
	constructor () public {
		_owner = msg.sender;
	}
 
 
 
	event createEvent(address owner, string tableName);
	event insertEvent(string logHash,string uploadTime,string msg,string DataObject,string verbs,string preFileHash,string preFilePath);
 
 
	// 创建日志表
	function create() public onlyOwner returns(int){
 
		TableFactory tf = TableFactory(0x1001);
		int count = tf.createTable("loginfo", "logHash", " uploadTime, msg, DataObject, verbs, preFileHash, preFilePath");
		emit createEvent(msg.sender, "loginfo");
		return count;
 
	}
	
	//插入日志
	function insert(string memory logHash,string memory uploadTime,string   memory msg,string memory DataObject,string memory verbs,string memory preFileHash,string memory preFilePath)public returns(int){
	    
	    TableFactory tf = TableFactory(0x1001);
	    Table table = tf.openTable("loginfo");
	    
	    Entry entry  = table.newEntry();
	    entry.set("logHash",logHash);
	    entry.set("uploadTime",uploadTime);
	    entry.set("msg",msg);
	    entry.set("DataObject",DataObject);
	    entry.set("verbs",verbs);
	    entry.set("preFileHash",preFileHash);
	    entry.set("preFilePath",preFilePath);
	    
	    int count = table.insert(logHash,entry);
	    emit insertEvent(logHash,uploadTime, msg, DataObject, verbs, preFileHash, preFilePath);
	    return count;
	    
	}
	
	// 查询日志操作
	function select(string memory logHash) public view returns(string memory,string memory,string memory,string memory,string memory,string memory,string memory){
 
		TableFactory tf = TableFactory(0x1001);
		Table table = tf.openTable("loginfo");
 
		//string memory userIDStr = addressToString(userID);
		Condition condition = table.newCondition();
		//condition.EQ("file_id", fileID);
 
		Entries entries = table.select(logHash, condition);

		if(entries.size() == 0){
		    return (logHash,"" ,"" ,"" ,"" ,"" ,"" );
		}
		else{
		    
		    return (logHash,
		    entries.get(0).getString("uploadTime"),
		    entries.get(0).getString("msg"),
		    entries.get(0).getString("DataObject"),
		    entries.get(0).getString("verbs"),
		    entries.get(0).getString("preFileHash"),
		    entries.get(0).getString("preFilePath"));
		}
 
	}


	
 
 
}
 
 
 
// table.sol，因为在webase-front的IDE中无法直接引用，所以将源码放到代码文件中
 
contract TableFactory {
 
    function openTable(string memory) public view returns (Table) {} //open table
 
    function createTable(string memory, string memory, string memory) public returns (int256) {} //create table
 
}
 
 
 
//select condition
 
contract Condition {
 
    function EQ(string memory, int256) public {}
 
    function EQ(string memory, string memory) public {}
 
 
 
    function NE(string memory, int256) public {}
 
    function NE(string memory, string memory) public {}
 
 
 
    function GT(string memory, int256) public {}
 
    function GE(string memory, int256) public {}
 
 
 
    function LT(string memory, int256) public {}
 
    function LE(string memory, int256) public {}
 
 
 
    function limit(int256) public {}
 
    function limit(int256, int256) public {}
 
}
 
 
 
//one record
 
contract Entry {
 
    function getInt(string memory) public view returns (int256) {}
 
    function getUInt(string memory) public view returns (int256) {}
 
    function getAddress(string memory) public view returns (address) {}
 
    function getBytes64(string memory) public view returns (bytes1[64] memory) {}
 
    function getBytes32(string memory) public view returns (bytes32) {}
 
    function getString(string memory) public view returns (string memory) {}
 
 
 
    function set(string memory, int256) public {}
 
    function set(string memory, uint256) public {}
     
    function set(string memory, string memory) public {}
 
    function set(string memory, address) public {}
 
}
 
 
 
//record sets
 
contract Entries {
 
    function get(int256) public view returns (Entry) {}
     
    function size() public view returns (int256) {}
 
}
 
 
 
//Table main contract
 
contract Table {
 
    function select(string memory, Condition) public view returns (Entries) {}
 
    function insert(string memory, Entry) public returns (int256) {}
 
    function update(string memory, Entry, Condition) public returns (int256) {}
 
    function remove(string memory, Condition) public returns (int256) {}
     
 
 
    function newEntry() public view returns (Entry) {}
 
    function newCondition() public view returns (Condition) {}
 
}
 
 
 
contract KVTableFactory {
 
    function openTable(string memory) public view returns (KVTable) {}
 
    function createTable(string memory, string memory, string memory) public returns (int256) {}
 
}
 
 
 
//KVTable per permiary key has only one Entry
 
contract KVTable {
 
    function get(string memory) public view returns (bool, Entry) {}
 
    function set(string memory, Entry) public returns (int256) {}
 
    function newEntry() public view returns (Entry) {}
 
}
