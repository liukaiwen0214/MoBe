import request from '~/utils/request'

export function pageBillsApi(data: {
  pageNum: number
  pageSize: number
  type?: string
  category?: string
  keyword?: string
  dateMode?: string
  quickDate?: string
  startDate?: string
  endDate?: string
  month?: string
}) {
  return request.post('/bills/page', data)
}
export function createBillApi(data: {
  type: string
  category: string
  amount: number | string
  recordDate: string
  remark?: string
}) {
  return request.post('/bills/create', data)
}
export function deleteRecordApi(data: {
  id: string
}) {
  return request.post(`/bills/delete/${data.id}`, data)
}
export function updateRecordApi(data: {
  id: string
}) {
  return request.post(`/bills/update/${data.id}`, data)
}
export function batchCreateBillsApi(data: {
  records: Array<{
    type: string
    category: string
    amount: number
    recordDate: string
    remark?: string
  }>
}) {
  return request.post('/bills/batch-create', data)
}