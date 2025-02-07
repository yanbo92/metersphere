<template>
  <ms-report-export-template
    :title="title"
    :report="report"
    :project-env-map="projectEnvMap"
    :pool-name="poolName"
    :mode="mode"
    :type="$t('report.api_test_report')">
    <ms-metric-chart :content="content" :is-export="true" :totalTime="totalTime" :report="report" />
    <div class="scenario-result" v-for="(scenario, index) in content.scenarios" :key="index" :scenario="scenario">
      <el-card>
        <template v-slot:header> {{ $t('api_report.scenario_name') }}：{{ scenario.name }} </template>
        <div
          class="ms-border clearfix"
          v-for="(request, index) in scenario.requestResults"
          :key="index"
          :request="request">
          <div class="request-top">
            <div>
              {{ getName(request.name) }}
            </div>
            <div class="url">
              {{ request.url }}
            </div>
          </div>
          <el-divider />
          <div class="request-middle">
            <api-report-request-header-item :title="$t('api_test.request.method')">
              <span class="method"> {{ request.method }}</span>
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.response_time')">
              {{ request.responseResult.responseTime }} ms
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.latency')">
              {{ request.responseResult.latency }} ms
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.request_size')">
              {{ request.requestSize }} bytes
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.response_size')">
              {{ request.responseResult.responseSize }} bytes
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.error')">
              {{ request.error }}
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.assertions')">
              {{ request.passAssertions + ' / ' + request.totalAssertions }}
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.response_code')" style="width: 300px">
              {{ request.responseResult.responseCode }}
            </api-report-request-header-item>

            <api-report-request-header-item :title="$t('api_report.result')" style="width: 50px">
              <el-tag v-if="request.unexecute">{{ $t('api_test.home_page.detail_card.unexecute') }} </el-tag>
              <el-tag v-else-if="!request.success && request.status && request.status === 'PENDING'">Pending </el-tag>
              <el-tag v-else-if="request.errorCode" class="ms-test-error_code"> FakeError </el-tag>
              <el-tag size="mini" type="success" v-else-if="request.success"> Success </el-tag>
              <el-tag size="mini" type="danger" v-else> Error </el-tag>
            </api-report-request-header-item>
          </div>
        </div>
      </el-card>
    </div>
  </ms-report-export-template>
</template>

<script>
import MsScenarioResult from './components/ScenarioResult';
import MsRequestResultTail from './components/RequestResultTail';
import ApiReportRequestHeaderItem from './ApiReportRequestHeaderItem';
import MsMetricChart from './components/MetricChart';
import MsReportTitle from 'metersphere-frontend/src/components/report/MsReportTitle';
import MsReportExportTemplate from 'metersphere-frontend/src/components/report/MsReportExportTemplate';
import MsAssertionResults from '../../../business/automation/report/components/AssertionResults';

export default {
  name: 'MsApiReportExport',
  components: {
    MsReportExportTemplate,
    MsReportTitle,
    MsMetricChart,
    ApiReportRequestHeaderItem,
    MsRequestResultTail,
    MsScenarioResult,
    MsAssertionResults,
  },
  props: {
    report: Object,
    content: Object,
    totalTime: Number,
    projectEnvMap: {},
    title: String,
    poolName: String,
    mode: String,
  },
  data() {
    return {};
  },
  methods: {
    getName(name) {
      if (name && name.indexOf('^@~@^') !== -1) {
        let arr = name.split('^@~@^');
        if (arr[arr.length - 1].indexOf('UUID=')) {
          return arr[arr.length - 1].split('UUID=')[0];
        }
        return arr[arr.length - 1];
      }
      return name;
    },
  },
};
</script>

<style scoped>
.scenario-result {
  margin-top: 20px;
  margin-bottom: 20px;
  min-width: 1200px;
}

.method {
  color: #1e90ff;
  font-size: 14px;
  font-weight: 500;
}

.request-top,
.request-bottom,
.request-middle {
  margin-left: 20px;
}

.url {
  color: #409eff;
  font-size: 14px;
  font-weight: bold;
  font-style: italic;
}

.el-card {
  border-style: none;
  padding: 10px 30px;
}

.request-top div {
  margin-top: 10px;
}

.ms-test-error_code {
  color: #f6972a;
  background-color: #fdf5ea;
  border-color: #fdf5ea;
}
</style>
