pragma solidity ^0.5.2;

// import "./Table.sol";

pragma experimental ABIEncoderV2;

contract OpLogByCRUD{

	address private _owner;

	modifier onlyOwner{
		require(_owner == msg.sender, "Auth: only owner is authorized");
		_;
	}

	constructor () public {
		_owner = msg.sender;
	}



	event createEvent(address owner, string tableName);
	event insertEvent(string logHash,string userID,string userIP, string opType,string opTime);




	// 创建日志表
	function create() public onlyOwner returns(int){

		TableFactory tf = TableFactory(0x1001);
		int count = tf.createTable("op_log2", "logHash", "userID, userIP,opType,opTime");
		emit createEvent(msg.sender, "op_log2");
		return count;

	}


	// 插入日志操作
	function insert(string memory logHash,string memory userID,string memory userIP, string memory opType,string memory opTime) public returns(int){


		TableFactory tf = TableFactory(0x1001);
		Table table = tf.openTable("op_log2");

		//string memory userIDStr = addressToString(userID);
		Entry entry = table.newEntry();
		entry.set("userID", userID);
		entry.set("userIP", userIP);
        entry.set("opType", opType);
        entry.set("opTime", opTime);

		int count = table.insert(logHash, entry);
		emit insertEvent(logHash,userID,userIP, opType,opTime);
		return count;

	}


	function addressToString(address  addr) private pure returns(string memory) {
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


	function encode(uint8  num) private pure returns(byte){
		// 0-9 -> 0-9
		if(num >= 0 && num <= 9){
			return byte(num + 48);
		}
		// 10-15 -> a-f
		if(num>=10 &&num<=15){
		    return byte(num + 87);
		}
		
	}



	// 查询日志操作
	function select(string memory logHash) public view returns(string memory,string[] memory,string[] memory,string[] memory, string[] memory){

		TableFactory tf = TableFactory(0x1001);
		Table table = tf.openTable("op_log2");

		//string memory userIDStr = addressToString(userID);
		Condition condition = table.newCondition();
		//condition.EQ("file_id", fileID);

		Entries entries = table.select(logHash, condition);
        // 获取记录集合的大小
        int size = entries.size();
        string[] memory userID_list = new string[](uint256(size));
        string[] memory userIP_list = new string[](uint256(size));
        string[] memory opType_list = new string[](uint256(size));
        string[] memory opTime_list = new string[](uint256(size));
    	if(entries.size() != 0){
    	    
          // 遍历记录集合
          // 将记录的三个字段值分别存放到三个数组中，方便方法返回查询的数据
            for(int i = 0; i< size; ++i) {
                // 根据索引获取记录集合中的记录
                Entry entry = entries.get(i);
                // 根据记录的字段名查询字段值
                // 注意字段值的类型不同需要选择相应的get方法
                userID_list[uint256(i)] =entry.getString("userID");
                userIP_list[uint256(i)] =entry.getString("userIP");
                opType_list[uint256(i)] =entry.getString("opType");
                opTime_list[uint256(i)] =entry.getString("opTime");
    	 	}
    	 	return (logHash,userID_list,userIP_list,opType_list,opTime_list);
      	}
      	else{

      	      return (logHash,userID_list,userIP_list,opType_list,opTime_list);
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