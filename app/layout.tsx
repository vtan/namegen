import { Lato } from "next/font/google";
import { config } from "@fortawesome/fontawesome-svg-core";
import "@fortawesome/fontawesome-svg-core/styles.css";
config.autoAddCss = false;

const lato = Lato({
  weight: ["400", "700"],
  subsets: ["latin"],
  display: "swap",
});

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={lato.className}>
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <title>Name generator</title>
        <meta property="og:title" content="Name generator" />
        <meta
          property="og:description"
          content="Generate random names based on US population data"
        />

        <link href="normalize.css" rel="stylesheet" />
        <link href="stylesheet.css" rel="stylesheet" />
        <link rel="icon" href="data:;base64,iVBORw0KGgo=" />
      </head>
      <body>{children}</body>
    </html>
  );
}
