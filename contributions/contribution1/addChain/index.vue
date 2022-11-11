<template> 
    <div class="app-container">
        <div class="table-container">
            <el-table ref="taskTable" :data="list" max-height="600" style="width: 100%;" v-loading="listLoading" border>
                <el-table-column label="数字摘要" align="center">
                    <template slot-scope="scope">{{scope.row.fileHash}}</template>
                </el-table-column>
                <el-table-column label="日志类型" align="center" :formatter="typeFormat">
                </el-table-column>
                <el-table-column label="文件路径" align="center">
                    <template slot-scope="scope">{{scope.row.filePath}}</template>
                </el-table-column>
                <el-table-column label="数据对象" align="center">
                    <template slot-scope="scope">{{scope.row.dataObj.map((item) => {return item.name}).join('，')}}</template>
                </el-table-column>
                <el-table-column label="任务进度" align="center">
                    <template slot-scope="scope"><el-progress :text-inside="true" :stroke-width="26" :percentage="scope.row.process * 100" :status="scope.row.process === 1 ? 'success' : ''"></el-progress></template>
                </el-table-column>
                <el-table-column label="操作" align="center">
                    <template slot-scope="scope">
                        <el-button size="mini" type="primary" @click="handleDetail(scope.$index, scope.row)">日志上链
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="pagination-container">
            <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
                layout="total, sizes,prev, pager, next,jumper" :current-page.sync="listQuery.current"
                :page-size="listQuery.size" :page-sizes="[10,15,20]" :total="total">
            </el-pagination>
        </div>

        <el-dialog :visible.sync="logVisible" width="60%" title="日志上链">
            <div class="table-container">
                <el-table ref="logTable" :data="logList" max-height="600" style="width: 100%;" v-loading="logLoading"
                    @selection-change="handleSelectionChange" border>
                    <el-table-column type="selection" width="55" align="center">
                    </el-table-column>
                    <el-table-column label="用户" align="center">
                        <template slot-scope="scope">{{scope.row.dataUserName}}</template>
                    </el-table-column>
                    <el-table-column label="操作行为" align="center">
                        <template slot-scope="scope">{{scope.row.verbs}}</template>
                    </el-table-column>
                    <el-table-column label="操作对象" align="center">
                        <template slot-scope="scope">{{scope.row.dataObject}}</template>
                    </el-table-column>
                    <el-table-column label="日志内容" align="center">
                        <template slot-scope="scope">{{scope.row.msg}}</template>
                    </el-table-column>
                    <el-table-column label="操作时间" align="center">
                        <template slot-scope="scope">{{scope.row.time}}</template>
                    </el-table-column>
                    <el-table-column label="上链时间" align="center" :formatter="timeFormat">
                        <!-- <template slot-scope="scope">{{scope.row.time}}</template> -->
                    </el-table-column>
                </el-table>
            </div>
            <div style="padding-top: 20px;">
                <el-button type="primary" @click="handleChain" size="small" style="float: right">立即上链</el-button>
            </div>
            <div style="clear: both;"></div>
        </el-dialog>

        <el-dialog :visible.sync="chainVisible" width="40%" title="日志上链">
            <div style="padding: 0 100px">
                <el-steps :space="50" direction="vertical" :active="active" finish-status="success" :align-center=true>
                    <el-step v-for="(step, index) in steps" :key="index" :title="step.title" :icon="step.icon"
                        v-if="step.display"></el-step>
                </el-steps>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import { getTaskList, getTaskById, intoChain } from '@/api/chain'
import { Message, MessageBox } from 'element-ui'
import dayjs from 'dayjs'

const defaultListQuery = {
    current: 1,
    size: 10
};
export default {
    name: 'addChain',
    data() {
        return {
            listQuery: Object.assign({}, defaultListQuery),
            list: null,
            logList: null,
            total: null,
            listLoading: false,
            logLoading: false,
            logVisible: false,
            chainVisible: false,
            disabled: false,
            mockList: [],
            loading: false,
            chainData: {
                taskId: null,
                logInfoId: []
            },
            chainError: null,
            active: 0,
            steps: [
                {
                    title: "正在生成数字摘要",
                    done: "成功生成数字摘要",
                    icon: "el-icon-loading",
                    display: true
                },
                {
                    title: "正在调用智能合约",
                    done: "成功调用智能合约",
                    icon: "el-icon-loading",
                    display: false
                },
                {
                    title: "正在进行日志上链",
                    done: "日志成功上链",
                    icon: "el-icon-loading",
                    display: false
                }
            ]
        }
    },
    created() {
        this.getList();
    },
    methods: {
        timeFormat(row, column) {
            if(row.blockChainTime === null) {
                return "未上链"
            } else {
                return dayjs(row.blockChainTime).format('YYYY-MM-DD HH:mm:ss')
            }
        },
        typeFormat(row, column) {
            if (row.type === 1) {
                return '安全日志'
            }
            if (row.type === 2) {
                return '流量日志'
            }
            if (row.type === 3) {
                return '系统日志'
            }
        },
        logTypeFormat(row, column) {
            if (row.logType === 4) {
                return 'syslog'
            }
            if (row.type === 5) {
                return 'kafka'
            }
            if (row.type === 3) {
                return '系统日志'
            }
        },
        handleResetSearch() {
            this.listQuery = Object.assign({}, defaultListQuery);
        },
        handleSizeChange(val) {
            this.listQuery.current = 1;
            this.listQuery.size = val;
            this.getList();
        },
        handleCurrentChange(val) {
            this.listQuery.current = val;
            this.getList();
        },
        getList() {
            this.listLoading = true;
            getTaskList(this.listQuery).then(res => {
                console.log(res.data);
                this.listLoading = false;
                this.list = res.data.records;
                this.total = parseInt(res.data.total);
            })
        },
        handleDetail(index, row) {
            this.logVisible = true;
            this.logLoading = true;
            this.steps = [
                {
                    title: "正在生成数字摘要",
                    done: "成功生成数字摘要",
                    icon: "el-icon-loading",
                    display: true
                },
                {
                    title: "正在调用智能合约",
                    done: "成功调用智能合约",
                    icon: "el-icon-loading",
                    display: false
                },
                {
                    title: "正在进行日志上链",
                    done: "日志成功上链",
                    icon: "el-icon-loading",
                    display: false
                }
            ]
            this.chainData.taskId = row.taskId;
            const taskId = row.taskId;
            getTaskById(taskId).then(res => {
                this.logList = res.data.data;
                this.logLoading = false;
                console.log(this.logList);
            })
        },
        handleSelectionChange(val) {
            // console.log(val)
            this.chainData.logInfoId = val.map((item) => {
                return item.id;
            })
        },
        handleChain() {
            this.logVisible = false;
            this.chainVisible = true;
            this.steps[0].display = true;
            setTimeout(() => {
                this.steps[0].icon = "";
                this.steps[0].title = this.steps[0].done;
                this.active = 1;
                this.steps[1].display = true;
                setTimeout(() => {
                    this.steps[1].icon = "";
                    this.steps[1].title = this.steps[1].done;
                    this.active = 2;
                    this.steps[2].display = true;
                    intoChain(this.chainData).then(res => {
                        console.log(res)
                        if (res.data.onError.length !== 0) {
                            this.chainError = res.data.onError;
                            Message({
                                message: "上链失败",
                                type: 'error',
                                duration: 3 * 1000
                            })
                        } else {
                            // console.log(this.steps);
                            this.steps[2].icon = "";
                            this.steps[2].title = this.steps[2].done;
                            this.active = 3;
                        }
                    })
                }, 2000)
            }, 2000)
        }
    }
}
</script>
<style>

</style>
  