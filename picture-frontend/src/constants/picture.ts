export const PIC_REVIEW_STATUS_ENUM = {
  PENDING: 0,
  PASS: 1,
  REJECT: 2,
};

export const PIC_REVIEW_STATUS_MAP = {
  0: '待审核',
  1: '审核通过',
  2: '审核不通过',
};

/**
 * 图片审核下拉表单选项
 */
export const PIC_REVIEW_STATUS_OPTIONS = Object.keys(PIC_REVIEW_STATUS_MAP).map((key) => {
  return {
    label: PIC_REVIEW_STATUS_MAP[key],
    value: key,
  }
})

/**
 * 图片编辑消息类型枚举
 */
export const PICTURE_EDIT_MESSAGE_TYPE_ENUM = {
  INFO: 'INFO',
  ERROR: 'ERROR',
  ENTER_EDIT: 'ENTER_EDIT',
  EXIT_EDIT: 'EXIT_EDIT',
  EDIT_ACTION: 'EDIT_ACTION',
};

/**
 * 图片编辑消息类型映射
 */
export const PICTURE_EDIT_MESSAGE_TYPE_MAP = {
  INFO: '发送通知',
  ERROR: '发送错误',
  ENTER_EDIT: '进入编辑状态',
  EXIT_EDIT: '退出编辑状态',
  EDIT_ACTION: '执行编辑操作',
};

/**
 * 图片编辑操作枚举
 */
export const PICTURE_EDIT_ACTION_ENUM = {
  ZOOM_IN: 'ZOOM_IN',
  ZOOM_OUT: 'ZOOM_OUT',
  ROTATE_LEFT: 'ROTATE_LEFT',
  ROTATE_RIGHT: 'ROTATE_RIGHT',
};

/**
 * 图片编辑操作映射
 */
export const PICTURE_EDIT_ACTION_MAP = {
  ZOOM_IN: '放大操作',
  ZOOM_OUT: '缩小操作',
  ROTATE_LEFT: '左旋操作',
  ROTATE_RIGHT: '右旋操作',
};

/**
 * Ai修改图片提示词
 */
export const AI_PICTURE_EDIT_PROMPT = {
  remove_watermark: '自动识别并移除图片中的水印，保持原始图像的质量。',
  expand: '智能扩展图像边界，生成符合原图风格的新内容。',
  super_resolution: '提高图像分辨率，使图像更加清晰。',
  doodle: '将线稿转换为彩色图像，根据提示词生成具体效果。',
  colorization: '为黑白或灰度图像添加自然的色彩。',
  stylization_local: '转换成法国绘本风格。'
};
