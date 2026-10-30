import { ChevronLeft, ChevronRight, Maximize2, Minimize2, X, ZoomIn, ZoomOut, RotateCcw, FileText } from "lucide-react";
import { useEffect, useRef, useState } from "react";

interface PdfViewerModalProps {
  title: string;
  url: string;
  isOpen: boolean;
  onClose: () => void;
}

export function PdfViewerModal({ title, url, isOpen, onClose }: PdfViewerModalProps) {
  const [zoom, setZoom] = useState(100);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [loading, setLoading] = useState(true);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen) {
      setLoading(true);
      setCurrentPage(1);
      setZoom(100);
    }
  }, [isOpen, url]);

  if (!isOpen) return null;

  const handleZoomIn = () => setZoom((prev) => Math.min(prev + 20, 200));
  const handleZoomOut = () => setZoom((prev) => Math.max(prev - 20, 60));
  const handleResetZoom = () => setZoom(100);

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen?.().then(() => setIsFullscreen(true)).catch(() => {});
    } else {
      document.exitFullscreen?.().then(() => setIsFullscreen(false)).catch(() => {});
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      dir="rtl"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-2 sm:p-4"
      onContextMenu={(e) => e.preventDefault()} // Browser deterrent against right-click save
    >
      <div
        ref={containerRef}
        className="flex h-full max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-2xl transition-all"
      >
        {/* Header Toolbar */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border bg-background/95 px-4 py-3 sm:px-6">
          <div className="flex items-center gap-2 min-w-0">
            <span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-accent/15 text-accent">
              <FileText className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <h2 className="truncate font-display text-base font-bold sm:text-lg text-foreground">{title}</h2>
              <p className="text-[11px] text-muted-foreground">منصة مِداد — عارض الملفات الآمن داخل التطبيق</p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-1.5 sm:gap-2">
            {/* Zoom Controls */}
            <div className="flex items-center rounded-lg border border-border bg-muted/50 p-0.5">
              <button
                type="button"
                onClick={handleZoomOut}
                title="تصغير"
                className="grid h-8 w-8 place-items-center rounded text-muted-foreground hover:bg-background hover:text-foreground"
              >
                <ZoomOut className="h-4 w-4" />
              </button>
              <span className="min-w-11 text-center text-xs font-semibold">{zoom}%</span>
              <button
                type="button"
                onClick={handleZoomIn}
                title="تكبير"
                className="grid h-8 w-8 place-items-center rounded text-muted-foreground hover:bg-background hover:text-foreground"
              >
                <ZoomIn className="h-4 w-4" />
              </button>
              <button
                type="button"
                onClick={handleResetZoom}
                title="إعادة ضبط"
                className="grid h-8 w-8 place-items-center rounded text-muted-foreground hover:bg-background hover:text-foreground"
              >
                <RotateCcw className="h-3.5 w-3.5" />
              </button>
            </div>

            {/* Fullscreen Toggle */}
            <button
              type="button"
              onClick={toggleFullscreen}
              title={isFullscreen ? "إنهاء ملء الشاشة" : "ملء الشاشة"}
              className="grid h-9 w-9 place-items-center rounded-lg border border-border bg-background text-foreground hover:bg-muted"
            >
              {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
            </button>

            {/* Close Button */}
            <button
              type="button"
              onClick={onClose}
              title="إغلاق العارض"
              className="grid h-9 w-9 place-items-center rounded-lg bg-destructive/10 text-destructive hover:bg-destructive/20 font-bold"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Content Viewer Body */}
        <div className="relative flex-1 overflow-auto bg-muted/30 p-2 sm:p-4 flex items-center justify-center">
          {loading && (
            <div className="absolute inset-0 z-10 flex flex-col items-center justify-center gap-2 bg-background/80 backdrop-blur-sm">
              <div className="h-8 w-8 animate-spin rounded-full border-3 border-accent border-t-transparent" />
              <p className="text-sm font-semibold text-muted-foreground">جاري تحميل المستند بأمان...</p>
            </div>
          )}

          <div
            style={{ transform: `scale(${zoom / 100})`, transformOrigin: "top center", transition: "transform 0.15s ease" }}
            className="w-full h-full min-h-[500px] flex items-center justify-center shadow-lg"
          >
            {/* Embedded secure sandbox frame without toolbars */}
            <iframe
              src={`${url}#toolbar=0&navpanes=0&scrollbar=1`}
              title={title}
              onLoad={() => setLoading(false)}
              className="h-full w-full rounded-xl border border-border bg-white shadow-inner"
              style={{ minHeight: "68vh" }}
            />
          </div>
        </div>

        {/* Footer info banner */}
        <div className="flex items-center justify-between border-t border-border bg-background px-4 py-2.5 text-xs text-muted-foreground">
          <span>🔒 مستند مخصص للقراءة الأكاديمية داخل المنصة</span>
          <span>حماية ضد التحميل المباشر مفعلة</span>
        </div>
      </div>
    </div>
  );
}
