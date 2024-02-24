module.exports = {
  output: 'export',
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'https://namegen.net/api/:path*' // Proxy to Backend
      }
    ]
  }
}
