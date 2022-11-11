pragma solidity ^0.5.2;
 
// import "./Table.sol";
 
contract LogByCRUD{
 
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
		int count = tf.createTable("log4", "logHash", "userID, userIP,RelativePath,logTye,fileSize,uploadTime");
		emit createEvent(msg.sender, "log4");
		return count;
 
	}
 
 
	// 插入log操作
	function insert(string memory logHash,string memory userID,string memory userIP,string memory RelativePath,string memory logTye,string memory fileSize,string memory uploadTime) public  returns(int){
 
 
		TableFactory tf = TableFactory(0x1001);
		Table table = tf.openTable("log4");
 
		//string memory userIDStr = addressToString(userID);
		Entry entry = table.newEntry();
		entry.set("userID", userID);
		entry.set("userIP", userIP);
		entry.set("RelativePath", RelativePath);
		entry.set("logTye",logTye);
		entry.set("fileSize",fileSize);
		entry.set("uploadTime",uploadTime);
		
 
		int count = table.insert(logHash, entry);
		emit insertEvent(logHash,userID, userIP,RelativePath,logTye,fileSize,uploadTime);
		return count;
 
	}
 
 
	function addressToString(address addr) private pure returns(string memory) {
		// Convert addr to bytes
		bytes20 value = bytes20(uint160(addr));
		bytes memory strBytes = new bytes(42);
		// Encode hex prefix
		strBytes[0] = '0';
		strBytes[1] = 'x';
 
		// Encode bytes usig hex encoding
		for(uint i = 0; i < 20; i++){
			uint8 byteValue = uint8(value[i]);
			strBytes[2 + (i<<1)] = encode(byteValue >> 4);
			strBytes[3 + (i<<1)] = encode(byteValue & 0x0f);
		}
 
		return string(strBytes);
 
 
 
	}
 
 
	function encode(uint8 num) private pure returns(byte){
		// 0-9 -> 0-9
		if(num >= 0 && num <= 9){
			return byte(num + 48);
		}
		// 10-15 -> a-f
		if(num>=10 && num<=15){
		    return byte(num + 87);
		}
	}
 

 
	// 查询日志操作
	function select(string memory logHash) public view returns(string memory,string memory,string memory, string memory,string memory,string memory,string memory){
 
		TableFactory tf = TableFactory(0x1001);
		Table table = tf.openTable("log4");
 
		//string memory userIDStr = addressToString(userID);
		Condition condition = table.newCondition();
		//condition.EQ("file_id", fileID);
 
		Entries entries = table.select(logHash, condition);
 
		if(entries.size() == 0){
		    return (logHash,"","","","","","");
		}
		else{
		    
		    return (logHash,
		    entries.get(0).getString("userID"),
		    entries.get(0).getString("userIP"),
		    entries.get(0).getString("RelativePath"),
		    entries.get(0).getString("logTye"),
		    entries.get(0).getString("fileSize"),
		    entries.get(0).getString("uploadTime"));
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