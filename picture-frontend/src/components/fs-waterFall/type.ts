export interface IImageItem {
  id: string | number;
  picWidth: number;
  picHeight: number;
  [key: string]: any; // 允许其他额外属性
}

export interface IWaterFallProps {
  column?: number; // 列数，默认4列
  gap?: number;    // 间距，默认16px
  request: (page: number, pageSize: number) => Promise<IImageItem[]>; // 数据请求方法
}
