<template>
   
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <div slot="header" class="clearfix">
        <el-button
          style="float: right"
          type="primary"
          @click="handleCheck()"
          size="small"
        >
          一致性校验
        </el-button>
        <el-button
          style="float: right; margin-right: 15px"
          type="primary"
          @click="handleTrace()"
          size="small"
        >
          开始溯源
        </el-button>
        <!-- <el-button
          style="float: right; margin-right: 15px"
          @click="handleResetSearch()"
          size="small"
        >
          重置
        </el-button> -->
        <el-button size="small" @click="changeItem" type="success">{{showObj === true ? '用户溯源' : '对象溯源'}}</el-button>
      </div>
      <div style="margin-top: 15px">
        <el-form
          :inline="true"
          size="small"
          label-width="140px"
        >
          <el-form-item label="数据对象：" v-show="showObj">
              <el-select v-model="dataObj" filterable reserve-keyword
                  placeholder="请选择数据对象" size="medium">
                  <el-option v-for="(item, index) in objOptions" :key="item.id" :label="item.name" :value="index">
                  </el-option>
              </el-select>
          </el-form-item>
          <el-form-item label="业务系统：" v-show="showObj">
              <el-input v-model="logtype" placeholder="请输入业务系统"></el-input>
          </el-form-item>
          <el-form-item label="业务用户：" v-show="!showObj">
              <el-select v-model="bussinessUser" filterable reserve-keyword
                  placeholder="请选择业务用户" size="medium">
                  <el-option v-for="(item, index) in userOptions" :key="item.id" :label="item.username" :value="index">
                  </el-option>
              </el-select>
          </el-form-item>
          <el-form-item label="开始时间：">
            <el-date-picker
              class="input-width"
              v-model="start"
              value-format="yyyy-MM-dd HH:mm:ss"
              type="datetime"
              placeholder="请选择时间"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item label="结束时间：">
            <el-date-picker
              class="input-width"
              v-model="end"
              value-format="yyyy-MM-dd HH:mm:ss"
              type="datetime"
              placeholder="请选择时间"
            >
            </el-date-picker>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    <div ref="graph" style="margin-top: 30px; height: 600px; width: 100%;"></div>
    <el-dialog :visible.sync="checkVisible" width="40%" title="一致性校验">
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
import { queryBusinessByUsername, getFileByObjectIds, getMsgByFileAndDataObj, getMsgByFileAndDataObjAndUsername, getUserByFile } from '@/api/analysis'
import { getObjList } from '@/api/dataObj'
import { getBussinessUserList } from '@/api/bussinessUser'
// import { getChainDataPage, getDetailByTask } from '@/api/trace'
import ElTableInfiniteScroll from "el-table-infinite-scroll"
// import G6 from "@antv/g6"

const data = {
  "id": "Modeling Methods",
  "children": [
    {
      "id": "Classification",
      "children": [
        {
          "id": "Logistic regression"
        },
        {
          "id": "Linear discriminant analysis"
        },
        {
          "id": "Rules"
        },
        {
          "id": "Decision trees"
        },
        {
          "id": "Naive Bayes"
        },
        {
          "id": "K nearest neighbor"
        },
        {
          "id": "Probabilistic neural network"
        },
        {
          "id": "Support vector machine"
        }
      ]
    },
    {
      "id": "Consensus",
      "children": [
        {
          "id": "Models diversity",
          "children": [
            {
              "id": "Different initializations"
            },
            {
              "id": "Different parameter choices"
            },
            {
              "id": "Different architectures"
            },
            {
              "id": "Different modeling methods"
            },
            {
              "id": "Different training sets"
            },
            {
              "id": "Different feature sets"
            }
          ]
        },
        {
          "id": "Methods",
          "children": [
            {
              "id": "Classifier selection"
            },
            {
              "id": "Classifier fusion"
            }
          ]
        },
        {
          "id": "Common",
          "children": [
            {
              "id": "Bagging",
              "style": {
                "fill": "red",
                "stroke": "red"
              }
            },
            {
              "id": "Boosting"
            },
            {
              "id": "AdaBoost"
            }
          ]
        }
      ]
    },
    {
      "id": "Regression",
      "children": [
        {
          "id": "Multiple linear regression"
        },
        {
          "id": "Partial least squares"
        },
        {
          "id": "Multi-layer feedforward neural network"
        },
        {
          "id": "General regression neural network"
        },
        {
          "id": "Support vector regression"
        }
      ]
    }
  ]
};
const defaultListQuery = {
  current: 1,
  size: 10,
  username: null,
  type: null,
  start: null,
  end: null,
  dataObj: null
};
const defaultLogDetail = {
  "id": '',
  "srcIp": '',
  "hashcode": '',
  "uploadTime": '',
  "filePath": '',
  "internetPath": ''
};
export default {
  name: 'traceQuery',
  directives: {
    "el-table-infinite-scroll": ElTableInfiniteScroll,
  },
  data() {
    return {
      objOptions: [],
      objListLoading: false,
      userOptions: [],
      userListLoading: false,
      bussinessUser: null,
      showObj: false,
      type: null,
      start: null,
      stratTime: null,
      endTime: null,
      logType: null,
      logtype: null,
      end: null,
      dataObj: null,
      myChart: null,
      checkVisible: false,
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
              title: "正在进行一致性校验",
              done: "一致性校验通过",
              icon: "el-icon-loading",
              display: false
          }
      ]
    }
  },
  mounted() {
    getObjList().then(res => {
      this.objOptions = res.data;
    })
    getBussinessUserList().then(res => {
      this.userOptions = res.data
    })
    this.getGraph();
  },
  methods: {
    getGraph() {
      const chart = this.$refs.graph
      this.myChart = this.$echarts.init(chart)
      
      this.myChart.on('click', (params) => {
        // 控制台打印数据的名称
        console.log(params);
        if (params.data.children.length !== 0) {
          return
        } else {
          if(params.data.level === 0) {
            getUserByFile(params.data.param).then(res => {
              console.log(res);
              params.data.children = res.data.map(item => {
                let param = Object.assign({}, params.data.param)
                param.userId = item.id
                param.stratTime = this.stratTime
                param.endTime = this.endTime
                return {
                  name: item.name,
                  level: 1,
                  param: param,
                  children: []
                }
              })
              params.data.collapsed=false;
              console.log(params);
              console.log(this.myChart.getOption().series[0].data);
              let data = this.myChart.getOption().series[0].data;
              // this.myChart.clear();
              this.updateGraph(data);
            })
          }
          if(params.data.level === 1) {
            getMsgByFileAndDataObjAndUsername(params.data.param).then(res => {
              console.log(res);
              let children = params.data.children;
              res.data.sort((a,b) => {
                return a.id - b.id
              })
              for(let item of res.data) {
                console.log(1);
                children.push({
                  id: item.id,
                  name: item.name,
                  verbs: item.verbs,
                  block_time: item.blockChainTime,
                  collapsed: false,
                  itemStyle: {
                    color: item.legitimate === true ? "skyblue" : "red",
                  },
                  label: {
                    formatter: (param) => {
                      return param.data.verbs
                    }
                  },
                  children: []
                })
                children = children[0].children
              }
              params.data.collapsed=false;
              console.log(params);
              console.log(this.myChart.getOption().series[0].data);
              let data = this.myChart.getOption().series[0].data;
              // this.myChart.clear();
              this.updateGraph(data);
            })
          }
          if (params.data.level === 2) {
            const param = {
              id: params.data.param.id,
              userId: params.data.param.userId,
              logType: this.logType
            }
            getFileByObjectIds(param).then(res => {
              console.log(res);
              params.data.children = res.data.map(item => {
                let param = {
                  objectId: params.data.param.id,
                  fileHash: item.id,
                  userId: params.data.param.userId,
                  startTime: this.stratTime,
                  endTime: this.endTime
                }
                // param.fileHash = item.id
                return {
                  name: item.name,
                  path: item.path,
                  logType: item.logType,
                  level: 3,
                  param: param,
                  children: []
                }
              })
              params.data.collapsed=false;
              let data = this.myChart.getOption().series[0].data;
              // this.myChart.clear();
              this.updateGraph(data);
            })
          }
          if(params.data.level === 3) {
            getMsgByFileAndDataObjAndUsername(params.data.param).then(res => {
              console.log(res);
              let children = params.data.children;
              res.data.sort((a,b) => {
                return a.id - b.id
              })
              for(let item of res.data) {
                console.log(1);
                children.push({
                  id: item.id,
                  name: item.name,
                  verbs: item.verbs,
                  block_time: item.blockChainTime,
                  collapsed: false,
                  itemStyle: {
                    color: item.legitimate === true ? "skyblue" : "red",
                  },
                  label: {
                    formatter: (param) => {
                      return param.data.verbs
                    }
                  },
                  children: []
                })
                children = children[0].children
              }
              params.data.collapsed=false;
              console.log(params);
              console.log(this.myChart.getOption().series[0].data);
              let data = this.myChart.getOption().series[0].data;
              // this.myChart.clear();
              this.updateGraph(data);
            })
          }
        }
      });
    },
    updateGraph(data) {
      this.myChart.clear();
      const option = {
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove',
          formatter: (param) => {
            console.log(param);
            if (param.data.level === 0 || param.data.level === 3) {
              return param.data.path + '<br />' + param.data.logType
            }
            return param.name
          }
        },
        series: [
          {
            type: 'tree',
            data: data,
            top: '1%',
            left: '7%',
            bottom: '1%',
            right: '20%',
            symbolSize: 15,
            label: {
              position: 'top',
              verticalAlign: 'middle',
              align: 'middle',
              fontSize: 14
            },
            itemStyle: {
              color: "skyblue"
            },
            leaves: {
              label: {
                position: 'top',
                verticalAlign: 'middle',
                align: 'middle'
              },
              itemStyle: {
                color: "skyblue"
              }
            },
            emphasis: {
              focus: 'descendant'
            },
            expandAndCollapse: true,
            animationDuration: 550,
            animationDurationUpdate: 200
          }
        ]
      }
      this.myChart.setOption(option)
    },
    changeItem() {
      this.showObj = !this.showObj
    },
    remoteUser(query) {
        this.userListLoading = true;
        getBussinessUserList(query).then(res => {
            this.userOptions = res.data;
            console.log(this.userOptions)
            this.userListLoading = false;
        })
    },
    remoteObj(query) {
        this.objListLoading = true;
        getObjList(query).then(res => {
            this.objOptions = res.data;
            this.objListLoading = false;
        })
    },
    handleCheck() {
      this.checkVisible = true;
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
              setTimeout(() => {
                this.steps[2].icon = "";
                this.steps[2].title = this.steps[2].done;
                this.active = 3;
              }, 1000)
          }, 2000)
      }, 2000)
    },
    handleTrace() {
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
              title: "正在进行一致性校验",
              done: "一致性校验通过",
              icon: "el-icon-loading",
              display: false
          }
      ]
      this.logType = this.logtype
      this.stratTime = this.start
      this.endTime = this.end
      if (this.showObj) {
        // this.logType = this.logtype
        let dataObject = Object.assign({}, this.objOptions[this.dataObj])
        getFileByObjectIds({id: dataObject.id, logType: this.logType}).then(res => {
          let files = res.data
          let graph = {
            name: dataObject.name,
            id: dataObject.id,
            children: files.map(item => {
              return {
                name: item.name,
                path: item.path,
                logType: item.logType,
                param: {
                  objectId: dataObject.id,
                  fileHash: item.id
                },
                // label: {
                //   formatter: (param) => {
                //     return param.data.path
                //   }
                // },
                level: 0,
                children: []
              }
            })
          }
          this.updateGraph([graph])
        })
      } else {
        let bussinessUser = Object.assign({}, this.userOptions[this.bussinessUser])
        console.log(bussinessUser);
        queryBusinessByUsername(bussinessUser.id).then(res => {
          console.log(res);
          let dataObjs = res.data
          let graph = {
            name: bussinessUser.username,
            id: bussinessUser.id,
            children: dataObjs.map(item => {
              return {
                name: item.name,
                param: {
                  id: item.id,
                  userId: bussinessUser.id,
                  username: bussinessUser.username
                },
                level: 2,
                children: []
              }
            })
          }
          this.updateGraph([graph])
        })
      }
    },
  }
}
</script>
<style>
.clearfix {
  clear: both;
}
</style>
