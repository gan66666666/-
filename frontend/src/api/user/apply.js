import userRequest from '../userRequest'

/**
 * 提交领养申请
 * @param {{applicantName: string, phone: string, petId: number, petName?: string, reason: string}} data
 */
export function createApply(data) {
  return userRequest.post('/apply', data)
}

