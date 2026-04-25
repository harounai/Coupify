// Lightweight pseudo-QR renderer. Deterministic from a string — purely visual for the demo.
export function QrCode({ value, size = 220 }: { value: string; size?: number }) {
  const grid = 25;
  const cell = size / grid;

  // Simple hash → bitmap
  const cells: boolean[] = [];
  let h = 0;
  for (let i = 0; i < value.length; i++) h = (h * 31 + value.charCodeAt(i)) | 0;
  for (let i = 0; i < grid * grid; i++) {
    h = (h * 1103515245 + 12345) & 0x7fffffff;
    cells.push((h & 7) > 3);
  }

  const isFinder = (r: number, c: number) => {
    const inBox = (br: number, bc: number) =>
      r >= br && r < br + 7 && c >= bc && c < bc + 7;
    return inBox(0, 0) || inBox(0, grid - 7) || inBox(grid - 7, 0);
  };
  const finderFill = (r: number, c: number) => {
    const inner = (br: number, bc: number) => {
      const rr = r - br;
      const cc = c - bc;
      if (rr < 0 || rr > 6 || cc < 0 || cc > 6) return null;
      if (rr === 0 || rr === 6 || cc === 0 || cc === 6) return true;
      if (rr >= 2 && rr <= 4 && cc >= 2 && cc <= 4) return true;
      return false;
    };
    return inner(0, 0) ?? inner(0, grid - 7) ?? inner(grid - 7, 0);
  };

  return (
    <svg
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
      className="rounded-2xl bg-white p-3 shadow-[var(--shadow-card)]"
    >
      {Array.from({ length: grid }).map((_, r) =>
        Array.from({ length: grid }).map((_, c) => {
          let on: boolean;
          if (isFinder(r, c)) {
            on = !!finderFill(r, c);
          } else {
            on = cells[r * grid + c];
          }
          if (!on) return null;
          return (
            <rect
              key={`${r}-${c}`}
              x={c * cell}
              y={r * cell}
              width={cell}
              height={cell}
              rx={cell * 0.15}
              fill="oklch(0.18 0.02 270)"
            />
          );
        })
      )}
    </svg>
  );
}
